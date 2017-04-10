package connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import connection.tables.FTableEntry;
import connection.tables.ForwardingTableService;
import connection.tables.NoEntryException;

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
	private static InetAddress myIP;

	public static final int DATA_PORT = 2000;
	public static final int DATA_ID = 150;
	public static final int DATA_TYPE_TEXT = 1;
	public static final int DATA_TYPE_IMAGE = 2;
	// list for packets needing acks. TODO

	// incoming data traffic
	private ServerSocket ssock;

	static {
		try {
			myIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	public static void sendText(InetAddress destIP, String msg) {
		JSONObject data = JSONservice.composeDataText(myIP, destIP, msg);
		sendData(destIP, data);
	}

	private static void sendData(InetAddress dest, JSONObject data) {
		try {
			// look for Forwarding table entry
			FTableEntry fe = ForwardingTableService.getEntry(dest);

			// initiate connection to next hop neighbor
			Socket sock = new Socket(fe.nextHopAddress, DATA_PORT);
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

			// write data to output stream
			out.println(data.toJSONString());

			// terminate connection
			out.close();
			sock.close();
		} catch (NoEntryException e) {
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
			 System.out.println("[Thread] Start listening for incoming data.");
			
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
				
				System.out.println("Received Data: " + json.toJSONString());

				if ((int) json.get("type") == DATA_ID) {
					// this is a data packet
					InetAddress destIP = InetAddress.getByName((String) json.get("destip"));
					if (destIP == myIP) {
						// this data packet is for me

						// TODO

					} else {
						// send data further
						sendData(destIP, json);
					}
				}
			}
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
		private boolean shutdown = false;
		// list for packets waiting for routing.
		private static ArrayList<JSONObject> waiting = new ArrayList<>();
		
		public static void addWaitingMsg(JSONObject json) {
			waiting.add(json);
		}

		@Override
		public void run() {
			System.out.println("[Thread] start checking waiting data packets.");
			
			while (!shutdown) {

				try {
					for (JSONObject j : waiting) {
						InetAddress destIP = InetAddress.getByName((String) j.get("destip"));
						if (ForwardingTableService.hasEntry(destIP)) {
							waiting.remove(j);
							DATAservice.sendData(destIP, j);
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
