package connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import connection.tables.ForwardingTableService;
import connection.tables.NoEntryException;
import connection.tables.RTableEntry;
import connection.tables.ReverseTableService;
import gui.StartPopup;

public class RREPservice implements Runnable {
	private static boolean shutdown = false;
	// RREP
	public static final int RREP_TRAFFIC_PORT = 2002;
	public static final int RREP_ID = 152;

	private static InetAddress myIP = Controller.myIP;
	private static ServerSocket ssock;
	// former received RREPs
	private static ArrayList<JSONObject> rcvdSSEPs = new ArrayList<>();

	static {
		try {
			ssock = new ServerSocket(RREP_TRAFFIC_PORT);
		} catch (IOException e) {
			new StartPopup("Application already running", "Another instance of this application is ",
					"already running. Please close it first!").setVisible(true);
		}

	}

	public static void sendRREP(InetAddress nextHop, InetAddress dest, InetAddress source, long sourceseq,
			long hopcount) {

		Controller.mainWindow.log("[RREP] Sending");

		JSONObject rrep = JSONservice.composeRREP(dest, source, sourceseq, hopcount);
		String msg = rrep.toJSONString();
		try {
			Socket sock = new Socket(dest.getHostAddress(), RREP_TRAFFIC_PORT);
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			out.println(msg);
			out.close();
			sock.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean isUpdate(JSONObject json) {
		try {
			int hopcount = (int) json.get("hopcount");
			int sourceSeq = (int) json.get("sourceseq");
			InetAddress destIP = InetAddress.getByName((String) json.get("destip"));

			boolean contains = false;
			for (JSONObject j : rcvdSSEPs) {
				InetAddress currentDestIP = InetAddress.getByName((String) j.get("destip"));
				int currentHopcount = (int) j.get("hopcount");
				int currentSourceSeq = (int) j.get("sourceseq");
				if (currentDestIP.equals(destIP)) {
					contains = true;
					// Already a RREP received for this destination, check seq#
					// and hopcount
					if (currentHopcount > hopcount || currentSourceSeq < sourceSeq) {
						return true;
					}
				}
			}
			return !contains;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void run() {
		try {
			Controller.mainWindow.log("[Thread] Start listening for incoming RREP packets.");

			while (!shutdown) {
				// wait for some incoming RREP connection
				Socket sock = ssock.accept();

				Controller.mainWindow.log("[RREP] Connection accepted");

				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				String input = in.readLine();
				JSONObject json = JSONservice.getJson(input);

				Controller.mainWindow.log("[RREP] Received " + input);

				// right protocol?
				if ((long) json.get("type") == RREP_ID) {

					// extract data
					InetAddress sourceIP = InetAddress.getByName((String) json.get("sourceip"));
					long sourceSeq = (long) json.get("sourceseq");
					InetAddress destIP = InetAddress.getByName((String) json.get("destip"));
					long hopcount = (long) json.get("hopcount");
					hopcount++;

					// Am I the destination?
					if (destIP.equals(myIP)) {
						ForwardingTableService.addEntry(sourceIP, sock.getInetAddress(), sourceSeq, hopcount);
					} else if (isUpdate(json)) { // Is this RREP new?
						RTableEntry re = ReverseTableService.getEntry(destIP);
						// add forwarding entry
						ForwardingTableService.addEntry(sourceIP, sock.getInetAddress(), sourceSeq, hopcount);
						// continues reversePath
						sendRREP(re.nextHopAddress, destIP, sourceIP, sourceSeq, hopcount);
					}
				} else {
					System.err.println("received invalid RREP packet.");
				}

				// close connection
				in.close();
				sock.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (NoEntryException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		shutdown = true;
	}
}
