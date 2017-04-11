package connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

import javax.activation.DataSource;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import connection.tables.FTableEntry;
import connection.tables.ForwardingTableService;
import connection.tables.NoEntryException;
import gui.StartPopup;

/**
 * The class which sends and receives text over the sockets' in- &
 * outputstreams. Both implemented on clientside and on serverside for each
 * connected client.
 * 
 * @author gereon
 *
 */
public class DATAservice implements Runnable {
	private static boolean shutdown = false;
	private static InetAddress myIP = Controller.myIP;

	public static final int DATA_PORT = 2000;
	public static final int DATA_ID = 150;
	public static final int DATA_TYPE_TEXT = 1;
	public static final int DATA_TYPE_IMAGE = 2;
	// list for packets needing acks. TODO

	// incoming data traffic
	private ServerSocket ssock;

	public static void sendText(InetAddress destIP, String msg) {
		JSONObject data = JSONservice.composeDataText(myIP, destIP, msg);
		sendData(destIP, data);
	}

	private static void sendData(InetAddress dest, JSONObject data) {
		try {
			String msg = (String) data.get("data");

			Controller.mainWindow.log("[DATA] Trying to send " + msg);

			// look for Forwarding table entry
			FTableEntry fe = ForwardingTableService.getEntry(dest);

			// initiate connection to next hop neighbor
			Socket sock = new Socket(fe.nextHopAddress, DATA_PORT);
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

			// write data to output stream
			out.println(data.toJSONString());

			Controller.mainWindow.log("[DATA] Successfully send!");

			// terminate connection
			out.close();
			sock.close();
		} catch (NoEntryException e) {
			Controller.mainWindow.log("[DATA] No route, initiate routing...");
			// no forwarding entry... initiate routing process...
			RREQservice.findRoute(dest);
			// waiting for routing...
			waitingChecker.addWaitingMsg(data);
		} catch (IOException e) {
			System.err.println("Data connection failed.");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			Controller.mainWindow.log("[Thread] Start listening for incoming data.");

			ssock = new ServerSocket(DATA_PORT);
			waitingChecker wc = new waitingChecker();
			Thread t = new Thread(wc);
			t.start();

			while (!shutdown) {
				// listen for incoming connections
				Socket sock = ssock.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				String input = in.readLine();
				JSONObject json = JSONservice.getJson(input);

				Controller.mainWindow.log("Received Data: " + json.toJSONString());

				if ((long) json.get("type") == DATA_ID) {
					// this is a data packet
					InetAddress destIP = InetAddress.getByName((String) json.get("destip"));
					if (destIP.equals(myIP)) {
						// this data packet is for me

						// TODO
						if ((long) json.get("datatype") == DATA_TYPE_TEXT) {
							int device = Integer.parseInt(((String) json.get("sourceip")).split("[.]")[3]);
							Controller.mainWindow.addMessage(device, (String) json.get("data"));
							Controller.mainWindow.refreshContactList();
						}

					} else {
						// send data further
						sendData(destIP, json);
					}
				}
			}
		} catch(BindException e) {
			// just silence it, it already caught by another thread
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	public void shutdown() {
		shutdown = true;
	}

	private static class waitingChecker implements Runnable {
		private static final long RREQ_TIMEOUT = 3;

		private boolean shutdown = false;
		// list for packets waiting for routing.
		private static HashMap<JSONObject, Long> waiting = new HashMap<>();

		public static void addWaitingMsg(JSONObject json) {
			Controller.mainWindow.log("[WAIT] Data packet added to waiting queue.");
			waiting.put(json, System.nanoTime());
		}

		@Override
		public void run() {
			Controller.mainWindow.log("[Thread] start checking waiting data packets.");

			while (!shutdown) {
				try {
					for (JSONObject j : waiting.keySet()) {
						InetAddress destIP = InetAddress.getByName((String) j.get("destip"));
						if (ForwardingTableService.hasEntry(destIP)) {
							DATAservice.sendData(destIP, j);
							waiting.remove(j);
						} else if (waiting.get(j) + RREQ_TIMEOUT * 1000000000 < System.nanoTime()) {
							DATAservice.sendData(destIP, j);
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

		public void shutdown() {
			shutdown = true;
		}
	}
}
