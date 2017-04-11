package applicationLayer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

	// list for packets waiting for routing.
	protected static HashMap<JSONObject, Long> waiting = new HashMap<>();

	public DataController() {
		// start threads
		DATAservice dataservice = new DATAservice();
		dataservice.addObserver(this);
		Thread data = new Thread(dataservice);
		data.start();

		Thread waitingList = new Thread(new waitingChecker());
		waitingList.start();

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

	private static void sendData(InetAddress destIP, JSONObject data) {
		try {
			// look for Forwarding table entry
			FTableEntry fe = ForwardingTableService.getEntry(destIP);
			// next hop address
			InetAddress nextHop = fe.nextHopAddress;
			// send Data
			
			newLog("[DATA] Success. ");
			
			DATAservice.sendData(nextHop, data.toJSONString());
		} catch (NoEntryException | IOException e) {
			// initiate routing
			NetworkController.findRoute(destIP);
			// let message wait
			waitingChecker.addWaitingMsg(data);
		}
	}
	
	// COMPOSITION	

	private static void manageMessage(JSONObject json) {
		try {
			// validate
			if ((long) json.get("type") == DATA_ID) {
				// this is a data packet
				InetAddress destIP = InetAddress.getByName((String) json.get("destip"));
				if (destIP.equals(getMyIP())) {
					// this data packet is for me

					if ((long) json.get("datatype") == DATA_TYPE_TEXT) {
						int device = Integer.parseInt(((String) json.get("sourceip")).split("[.]")[3]);
						Controller.receivedMessage(device, (String) json.get("data"));
					}

				} else {
					// send data further
					sendData(destIP, json);
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
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
				manageMessage(json);
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}
	}

	private static class waitingChecker implements Runnable {
		private static final long RREQ_TIMEOUT = 3;

		public static void addWaitingMsg(JSONObject json) {
			waiting.put(json, System.nanoTime());
		}

		@Override
		public void run() {
			while (true) {
				try {
					for (JSONObject j : waiting.keySet()) {
						InetAddress destIP = InetAddress.getByName((String) j.get("destip"));
						if (ForwardingTableService.hasEntry(destIP)
								|| (waiting.get(j) + RREQ_TIMEOUT * 1000000000 < System.nanoTime())) {
							sendData(destIP, j);
							waiting.remove(j);
						}
					}
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (ConcurrentModificationException e) {
					// nothing
				}
			}
		}
	}
}
