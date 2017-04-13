package networkLayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import applicationLayer.JSONservice;
import controller.Controller;
import networkLayer.tables.FTableEntry;
import networkLayer.tables.ForwardingTableService;
import networkLayer.tables.NoEntryException;
import networkLayer.tables.RTableEntry;
import networkLayer.tables.ReverseTableService;

public class NetworkController implements Observer {
	// my information
	private static long mySeq = 0;
	private static InetAddress myIP;
	private static int myBroadcastID = 0;
	public static final int RREP_ID = 152;
	public static final int RREQ_ID = 151;
	public static final int RERR_ID = 153;

	// former received RREPs
	protected static ArrayList<JSONObject> rcvdRerrs = new ArrayList<>();
	// former received RREPs
	private static ArrayList<JSONObject> rcvdSSEPs = new ArrayList<>();
	// neighbours (Hello protocol)
	protected static HashMap<InetAddress, LocalTime> neighbours = new HashMap<>();
	// former received RREQs
	private static ArrayList<JSONObject> rcvdRREQs = new ArrayList<>();

	static {
		try {
			myIP = InetAddress.getByName(getAddress("192.168.5."));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public NetworkController() {
		HELLOservice helloservice = new HELLOservice();
		helloservice.addObserver(this);
		Thread hello = new Thread(helloservice);
		hello.start();

		RERRservice rerrservice = new RERRservice();
		rerrservice.addObserver(this);
		Thread rerr = new Thread(rerrservice);
		rerr.start();

		RREQservice rreqservice = new RREQservice();
		rreqservice.addObserver(this);
		Thread rreq = new Thread(rreqservice);
		rreq.start();

		RREPservice rrepservice = new RREPservice();
		rrepservice.addObserver(this);
		Thread rrep = new Thread(rrepservice);
		rrep.start();

		Thread ftable = new Thread(new ForwardingTableService());
		ftable.start();

		Thread rtable = new Thread(new ReverseTableService());
		rtable.start();

		Controller.mainWindow.log("[Thread] Network Layer started.");
	}

	@SuppressWarnings("rawtypes")
	public static String getAddress(String firstPart) {
		Enumeration e = null;
		try {
			e = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException exc) {

		}
		while (e.hasMoreElements()) {
			NetworkInterface n = (NetworkInterface) e.nextElement();
			Enumeration ee = n.getInetAddresses();
			while (ee.hasMoreElements()) {
				InetAddress i = (InetAddress) ee.nextElement();
				String address = i.getHostAddress();
				if (address.startsWith(firstPart)) {
					return address;
				}
			}
		}
		return null;
	}

	public static synchronized void incrementSeq() {
		Controller.mainWindow.log("[Network] Incrementing sequence number");
		mySeq++;
	}

	public static InetAddress getMyIP() {
		return myIP;
	}

	public static long getSeq() {
		return mySeq;
	}

	public static HashMap<InetAddress, LocalTime> getNeighbours() {
		return neighbours;
	}

	public static boolean hasRoute(InetAddress destIP) {
		return ForwardingTableService.hasEntry(destIP);
	}

	@Override
	public synchronized void update(Observable arg0, Object arg1) {
		if (arg0 instanceof RERRservice) {
			receivedRERR((DatagramPacket) arg1);
		} else if (arg0 instanceof RREPservice) {
			receivedRREP((Socket) arg1);
		} else if (arg0 instanceof HELLOservice) {
			receivedHELLO((DatagramPacket) arg1);
		} else if (arg0 instanceof RREQservice) {
			receivedRREQ((DatagramPacket) arg1);
		}
	}

	private static void receivedHELLO(DatagramPacket p) {
		// add to list
		InetAddress source = p.getAddress();
		if (!source.equals(getMyIP())) {
			int retries = HELLOservice.HELLO_LOSS + 1;
			int sec = HELLOservice.HELLO_INTERVAL;
			neighbours.put(source, LocalTime.now().plusSeconds(retries * sec));
			System.out.println("new neighbour: " + source.getHostAddress());
		}
	}

	@SuppressWarnings("unchecked")
	private static void receivedRERR(DatagramPacket p) {
		try {
			// extract Data
			String msg = new String(p.getData());
			// get JSON
			JSONObject json = JSONservice.getJson(msg);

			// check if already received before
			if (!rcvdRerrs.contains(json)) {
				// add to list of received packets
				rcvdRerrs.add(json);
				// extract data
				InetAddress sourceIP = InetAddress.getByName((String) json.get("sourceip"));
				long sourceSeq = (long) json.get("sourceseq");

				newLog("[RERR] Received from: " + sourceIP.getHostAddress());

				// get unreachable array
				JSONArray array = (JSONArray) json.get("unreachable");
				String[] strings = (String[]) array.toArray(new String[0]);
				InetAddress[] unreachable = new InetAddress[array.size()];
				for (int i = 0; i < strings.length; i++) {
					unreachable[i] = InetAddress.getByName(strings[i]);
				}

				// update ftable with source seq
				if (ForwardingTableService.hasEntry(sourceIP)) {
					try {
						FTableEntry fe = ForwardingTableService.getEntry(sourceIP);
						fe.destinationSequenceNumber = sourceSeq;
					} catch (NoEntryException e) {
						e.printStackTrace();
					}
				}

				// update forwarding entries
				for (InetAddress i : unreachable) {
					if (ForwardingTableService.hasEntry(i)) {
						try {
							FTableEntry fe = ForwardingTableService.getEntry(i);
							fe.hopcount = 100000;
						} catch (NoEntryException e) {
							e.printStackTrace();
						}
					}
				}

				// rebroadcast RERR
				RERRservice.rebroadcast(p);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void receivedRREP(Socket sock) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			// read data
			String input = in.readLine();
			JSONObject json = JSONservice.getJson(input);

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
				} else if (isRREPNew(json)) { // Is this RREP new?
					RTableEntry re = ReverseTableService.getEntry(destIP);
					// add forwarding entry to RREP source
					ForwardingTableService.addEntry(sourceIP, sock.getInetAddress(), sourceSeq, hopcount);
					// add forwarding entry to RREQ source
					ForwardingTableService.addEntry(re.sourceAddress, re.nextHopAddress, re.sourceSequenceNumber,
							re.hopsToSource);
					// continues reversePath
					sendRREP(re.nextHopAddress, destIP, sourceIP, sourceSeq, hopcount);
				}
			} else {
				System.err.println("received invalid RREP packet.");
			}

			// close connection
			in.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (NoEntryException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static void receivedRREQ(DatagramPacket p) {
		try {
			// extract Data
			String msg = new String(p.getData());

			Controller.mainWindow.log("[RREQ] Received " + msg);

			JSONObject json = JSONservice.getJson(msg);

			// right protocol?
			if ((long) json.get("type") == RREQ_ID) {
				InetAddress neighbor = p.getAddress();
				InetAddress sourceIP = InetAddress.getByName((String) json.get("sourceip"));
				long sourceSeq = (long) (json.get("sourceseq"));
				InetAddress destIP = InetAddress.getByName((String) json.get("destip"));
				long destSeq = (long) (json.get("destseq"));
				long hopcount = (long) (json.get("hopcount"));

				// Did i receive this RREQ before?
				if (receivedBefore(json) || (myIP.equals(sourceIP))) {
					// ignore packet
				} else {
					rcvdRREQs.add(json);

					// Am I the RREQ destination?
					if (destIP.equals(myIP)) {
						// add ftable entry
						ForwardingTableService.addEntry(sourceIP, neighbor, sourceSeq, hopcount);
						// send RREP
						sendRREP(neighbor, sourceIP, myIP, NetworkController.getSeq(), 0);
					} else {
						// Do I know a good enough route?
						if (ForwardingTableService.hasEntry(destIP)
								&& ForwardingTableService.getEntry(destIP).destinationSequenceNumber >= destSeq) {

							FTableEntry fe = ForwardingTableService.getEntry(destIP);
							sendRREP(neighbor, sourceIP, destIP, fe.destinationSequenceNumber, fe.hopcount);

						} else {
							// create RTable entry
							ReverseTableService.addEntry(sourceIP, neighbor, sourceSeq, hopcount);

							// increment hopcount and re-broadcast
							json.put("hopcount", hopcount + 1);
							RREQservice.sendRREQ(json.toJSONString());
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoEntryException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public static void findRoute(InetAddress destIP) {
		newLog("[Network] Finding route to: " + destIP.getHostAddress());

		if (!ForwardingTableService.hasEntry(destIP)) {
			// send a new RREQ
			myBroadcastID++;
			sendRREQ(myIP, NetworkController.getSeq(), myBroadcastID, destIP, 0, 0);
		}
	}

	public static void sendRERR(InetAddress failureLink) {
		// get the unreachable nodes
		InetAddress[] unreachables = ForwardingTableService.getNodes(failureLink);
		// get json message
		JSONObject rerr = JSONservice.composeRERR(myIP, NetworkController.getSeq(), unreachables);
		// convert to String
		String msg = rerr.toJSONString();
		// send RERR
		RERRservice.sendRERR(msg);
	}

	public static void sendRREP(InetAddress nextHop, InetAddress dest, InetAddress source, long sourceseq,
			long hopcount) {
		// get json
		JSONObject rrep = JSONservice.composeRREP(dest, source, sourceseq, hopcount);
		// convert to String
		String msg = rrep.toJSONString();
		// send RREP
		RREPservice.sendRREP(dest, msg);
	}

	public static void sendRREQ(InetAddress sourceIP, long sourceSeq, int broadcastID, InetAddress destIP, long destSeq,
			long hopCount) {
		// get json
		JSONObject rreq = JSONservice.composeRREQ(sourceIP, sourceSeq, broadcastID, destIP, destSeq, hopCount);
		// convert to String
		String msg = rreq.toJSONString();
		// send
		RREQservice.sendRREQ(msg);
	}

	public static boolean isRREPNew(JSONObject json) {
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

	private static boolean receivedBefore(JSONObject json) {
		String sourceIP = (String) json.get("sourceip");
		long broadcastID = (long) (json.get("broadcastid"));

		for (JSONObject j : rcvdRREQs) {
			String currentIP = (String) j.get("sourceip");
			long currentID = (long) (j.get("broadcastid"));
			if (currentIP.equals(sourceIP) && currentID == broadcastID) {
				// source IP and broadcast ID identify a RREQ request
				return true;
			}
		}
		return false;
	}

	public static void newLog(String s) {
		Controller.mainWindow.log(s);
	}
}
