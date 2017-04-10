package connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import connection.tables.FTableEntry;
import connection.tables.ForwardingTableService;
import connection.tables.NoEntryException;
import connection.tables.ReverseTableService;

public class RREQservice extends Observable implements Runnable {
	private static boolean shutdown = false;
	// RREQ
	public static final String RREQ_TRAFFIC_ADDRESS = "228.0.0.1";
	public static final int RREQ_TRAFFIC_PORT = 2001;
	public static final int RREQ_ID = 151;
	// Socket
	private static MulticastSocket msock;
	private static InetAddress myIP;
	private static InetAddress group;
	// former received RREQs
	private static ArrayList<JSONObject> rcvdRREQs = new ArrayList<>();
	// my information
	private static int mySeq = 0;
	private static int myBroadcastID = 0;

	static {
		try {
			myIP = InetAddress.getLocalHost();
			group = InetAddress.getByName(RREQ_TRAFFIC_ADDRESS);
			msock = new MulticastSocket(RREQ_TRAFFIC_PORT);
			msock.joinGroup(group);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void findRoute(InetAddress destIP) {
		if (!ForwardingTableService.hasEntry(destIP)) {
			// send a new RREQ
			myBroadcastID++;
			JSONObject rreq = JSONservice.composeRREQ(myIP, mySeq, myBroadcastID, destIP, 0, 0);
			sendRREQ(rreq);
		}
	}

	private static void sendRREQ(JSONObject json) {
		String msg = json.toJSONString();
		System.out.println("[CommunicationService] Sending: " + msg);

		try {
			DatagramPacket d = new DatagramPacket(msg.getBytes(), msg.length(), group, RREQ_TRAFFIC_PORT);

			msock.send(d);
			System.out.println("Success.");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean receivedBefore(JSONObject json) {
		String sourceIP = (String) json.get("sourceip");
		int broadcastID = (int) (json.get("broadcastid"));

		for (JSONObject j : rcvdRREQs) {
			String currentIP = (String) j.get("sourceip");
			int currentID = (int) (j.get("broadcastid"));
			if (currentIP.equals(sourceIP) && currentID == broadcastID) {
				// source IP and broadcast ID identify a RREQ request
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			System.out.println("[Thread] Start listening for incoming RREQ packets.");
			
			// listen for RREQs
			while (!shutdown) {
				// receive packet
				byte[] buffer = new byte[1000];
				DatagramPacket p = new DatagramPacket(buffer, buffer.length);
				msock.receive(p);

				// extract Data
				String msg = p.getData().toString();
				JSONObject json = JSONservice.getJson(msg);

				// right protocol?
				if ((int) json.get("type") == RREQ_ID) {
					InetAddress neighbor = p.getAddress();
					InetAddress sourceIP = InetAddress.getByName((String) json.get("sourceip"));
					int sourceSeq = (int) (json.get("sourceseq"));
					InetAddress destIP = InetAddress.getByName((String) json.get("destip"));
					int destSeq = (int) (json.get("destseq"));
					int hopcount = (int) (json.get("hopcount"));

					// Did i receive this RREQ before?
					if (receivedBefore(json)) {
						// ignore packet
					} else {
						// Am I the RREQ destination?
						if (destIP == myIP) {
							// send RREP
							RREPservice.sendRREP(neighbor, sourceIP, myIP, mySeq, 0);

						} else {
							// Do I know a good enough route?
							if (ForwardingTableService.hasEntry(destIP)
									&& ForwardingTableService.getEntry(destIP).destinationSequenceNumber >= destSeq) {

								FTableEntry fe = ForwardingTableService.getEntry(destIP);
								RREPservice.sendRREP(neighbor, sourceIP, destIP, fe.destinationSequenceNumber,
										fe.hopcount);

							} else {
								// create RTable entry
								ReverseTableService.addEntry(sourceIP, neighbor, sourceSeq, hopcount);

								// increment hopcount and re-broadcast
								json.put("hopcount", hopcount + 1);
								sendRREQ(json);
							}
						}
					}
				} else {
					System.err.println("Received invalid RREQ packet.");
				}

			}
			msock.leaveGroup(group);
			msock.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (NoEntryException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		shutdown = true;
	}
}
