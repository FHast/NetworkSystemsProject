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
public class Peer implements Runnable {
	private boolean shutdown = false;
	private InetAddress myIP;

	public static final int DATA_PORT = 2000;
	public static final int DATA_ID = 150;
	public static final int DATA_TYPE_TEXT = 1;
	public static final int DATA_TYPE_IMAGE = 2;
	// list for packets waiting for routing.
	private ArrayList<JSONObject> waiting = new ArrayList<>();
	// list for packets needing acks. TODO

	// incoming data traffic
	private ServerSocket ssock;

	public Peer() {
		try {
			myIP = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	public void sendText(InetAddress destIP, String msg) {
		JSONObject data = JSONservice.composeDataText(myIP, destIP, msg);
		sendData(destIP, data);
	}

	private void sendData(InetAddress dest, JSONObject data) {
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
			waiting.add(data);
		} catch (IOException e) {
			System.err.println("Data connection failed.");
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			ssock = new ServerSocket(DATA_PORT);

			while (!shutdown) {
				// listen for incoming connections
				Socket sock = ssock.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				String input = in.readLine();
				JSONObject json = JSONservice.getJson(input);

				// TODO
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
}
