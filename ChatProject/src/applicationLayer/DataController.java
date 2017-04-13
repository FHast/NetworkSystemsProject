package applicationLayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import controller.Controller;
import networkLayer.NetworkController;
import networkLayer.tables.FTableEntry;
import networkLayer.tables.ForwardingTableService;
import networkLayer.tables.NoEntryException;

public class DataController implements Observer {

	public static final int DATA_PORT = 2000;
	public static final int DATA_ID = 150;
	public static final int DATA_TYPE_TEXT = 1;
	public static final int DATA_TYPE_IMAGE = 2;
	public static final int DATA_TYPE_ACK = 0;

	// list for packets waiting for routing.
	protected static HashMap<JSONObject, Long> waiting = new HashMap<>();
	// acknowledgements
	private static HashMap<JSONObject, String> needAck = new HashMap<>();

	public DataController() {
		// start threads
		DATAservice dataservice = new DATAservice();
		dataservice.addObserver(this);
		Thread data = new Thread(dataservice);
		data.start();

		Thread waitingList = new Thread(new waitingChecker());
		waitingList.start();

		Thread ack = new Thread(new ackChecker());
		ack.start();

		// start networking layer
		new NetworkController();

		Controller.mainWindow.log("[Thread] Application Layer started.");
	}

	public static InetAddress getMyIP() {
		return NetworkController.getMyIP();
	}

	public static void sendMessage(InetAddress destIP, String message) {
		newLog("[DATA] Trying to send: " + message);

		// data to send
		JSONObject data = JSONservice.composeDataText(getMyIP(), destIP, message);
		// send data
		sendData(destIP, data);
	}

	public static void sendFile(InetAddress destIP, File file) {
		newLog("[DATA] Trying to send: " + file.getName() + " / " + file.getPath());

		// get data
		String url = file.getPath();
		String appendix = FileService.getAppendix(url);
		String data = FileService.fileToString(url);
		// get json
		JSONObject json = JSONservice.composeDataFile(getMyIP(), destIP, data, appendix);
		// send data
		sendData(destIP, json);

	}

	private static void sendData(InetAddress destIP, JSONObject data) {
		try {
			// look for Forwarding table entry
			FTableEntry fe = ForwardingTableService.getEntry(destIP);
			// renew entry
			ForwardingTableService.renewEntry(destIP);
			// next hop address
			InetAddress nextHop = fe.nextHopAddress;
			// add to the needAck list
			needAck.put(data, HashService.simpleHash(data.toJSONString()));
			// send Data
			
			newLog("[DATA] Success. ");

			DATAservice.sendData(nextHop, data.toJSONString());
		} catch (NoEntryException | IOException e) {
			// initiate routing
			NetworkController.findRoute(destIP);
			// let message wait
			waiting.put(data, System.nanoTime());
		}
	}

	private static void sendAck(InetAddress destIP, JSONObject toBeAcknowledged) {
		try {
			DataController.newLog("[ACK] Acknowledging: " + (String) toBeAcknowledged.get("data"));

			String hash = HashService.simpleHash(toBeAcknowledged.toJSONString());
			// to json
			JSONObject data = JSONservice.composeDataAck(getMyIP(), destIP, hash);
			// look for Forwarding table entry
			FTableEntry fe = ForwardingTableService.getEntry(destIP);
			// send
			DATAservice.sendData(fe.nextHopAddress, data.toJSONString());
		} catch (IOException | NoEntryException e) {
			NetworkController.findRoute(destIP);
		}
	}

	// COMPOSITION

	private static void receivedMessage(JSONObject json) {
		try {
			// validate
			if ((long) json.get("type") == DATA_ID) {
				// this is a data packet
				InetAddress destIP = InetAddress.getByName((String) json.get("destip"));
				InetAddress sourceIP = InetAddress.getByName((String) json.get("sourceip"));
				if (destIP.equals(getMyIP())) {
					// this data packet is for me

					if (((String) json.get("datatype")).equals("" + DATA_TYPE_ACK)) {
						String hash = (String) json.get("data");
						// manage ack
						receivedACK(hash);

					} else {
						// distinguish different data
						int device = Integer.parseInt(((String) json.get("sourceip")).split("[.]")[3]);
						if (((String) json.get("datatype")).equals("" + DATA_TYPE_TEXT)) {
							// send to controller
							Controller.receivedMessage(device, (String) json.get("data"));
						} else {
							try {
								String appendix = (String) json.get("datatype");
								String data = (String) json.get("data");
								// get file
								String path = FileService.stringToFile(data, appendix);

								newLog("[DATA] Received file (" + appendix + ") created: " + path);

								// send to controller
								Controller.receivedFile(device, path);

							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						}

						// send ack
						sendAck(sourceIP, json);
					}

				} else {
					// send data further
					newLog("[DATA] give further to: " + destIP.getHostAddress());
					sendData(destIP, json);
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static void receivedACK(String hash) {
		try {
			for (JSONObject j : needAck.keySet()) {
				if (needAck.get(j).equals(hash)) {
					DataController.newLog("[ACK] message acknowledged: " + (String) j.get("data"));

					// acknowledged
					needAck.remove(j);
				}
			}
		} catch (ConcurrentModificationException e) {
			// nothing
		}
	}

	public static void newLog(String s) {
		Controller.mainWindow.log(s);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg0 instanceof DATAservice) {
			try {
				JSONObject json = JSONservice.getJson((String) arg1);
				receivedMessage(json);
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}
	}

	private static class ackChecker implements Runnable {

		@Override
		public void run() {
			for (JSONObject j : needAck.keySet()) {
				LocalTime timestamp = LocalTime.parse((String) j.get("timestamp"));
				if (timestamp.plusSeconds(3).isBefore(LocalTime.now())) {
					try {
						// ack timed out
						InetAddress destIP = InetAddress.getByName((String) j.get("destip"));
						// remove from ftable
						ForwardingTableService.removeEntry(destIP);
						// send again
						sendData(destIP, j);
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	private static class waitingChecker implements Runnable {
		private static final long RREQ_TIMEOUT = 3;

		@Override
		public void run() {
			while (true) {
				try {
					for (JSONObject j : waiting.keySet()) {
						// get dest
						InetAddress destIP = InetAddress.getByName((String) j.get("destip"));
						// check if route available
						if (ForwardingTableService.hasEntry(destIP)
								|| (waiting.get(j) + RREQ_TIMEOUT * 1000000000 < System.nanoTime())) {
							waiting.remove(j);
							sendData(destIP, j);
						}
					}
					Thread.sleep(100);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (ConcurrentModificationException | NullPointerException e) {
					// nothing
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
