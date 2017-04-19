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
import java.security.PublicKey;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.crypto.SecretKey;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import applicationLayer.DataController;
import controller.Controller;
import networkLayer.tables.FTableEntry;
import networkLayer.tables.ForwardingTableService;
import networkLayer.tables.NoEntryException;
import networkLayer.tables.RTableEntry;
import networkLayer.tables.ReverseTableService;
import security.AES;
import security.RSA;

public class NetworkController implements Observer {
	public static final int TYPE_DATA = 0;
	public static final int TYPE_HELLO = 1;
	public static final int TYPE_RREQ = 2;
	public static final int TYPE_RERR = 3;
	public static final int TYPE_RREP = 4;
	public static final int TYPE_ACK = 5;

	public static InetAddress myIP;

	// HELLO
	private static HashMap<InetAddress, LocalTime> neighbours;
	private static final int HELLO_LOSS = 3;
	private static final int HELLO_INTERVAL = 1;
	// RREQ
	private static ArrayList<JSONObject> receivedRREQList;
	private static int broadcastID = 0;
	// RERR
	private static ArrayList<JSONObject> receivedRERRList;
	// DATA
	private static final int TIMEOUT_WAITING = 10;
	private static final int TIMEOUT_ACK = 10;
	private static HashMap<JSONObject, LocalTime> waiting;
	private static HashMap<JSONObject, LocalTime> needAck;

	static {
		// myIP
		try {
			myIP = InetAddress.getByName(getAddress("192.168.5."));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public NetworkController() {
		// initialize
		neighbours = new HashMap<>();
		receivedRREQList = new ArrayList<>();
		receivedRERRList = new ArrayList<>();
		waiting = new HashMap<>();
		needAck = new HashMap<>();

		// start Thread
		new Thread(new SayHello()).start();
		new Thread(new CheckLists()).start();
		new Thread(new ForwardingTableService()).start();
		new Thread(new ReverseTableService()).start();

		Multicast m = new Multicast();
		m.addObserver(this);
		new Thread(m).start();

		Unicast u = new Unicast();
		u.addObserver(this);
		new Thread(u).start();

		newLog("[Thread] Network Layer started.");
	}

	public static void newLog(String s) {
		Controller.mainWindow.log(s);
	}

	// HELLO

	private static void receivedHELLO(JSONObject json) {
		try {
			InetAddress source = InetAddress.getByName((String) json.get("sourceip"));
			// dont add yourself...
			if (!source.equals(myIP)) {
				// print to log
				if (!neighbours.containsKey(source)) {
					newLog("[HELLO] Detected new neighbour: " + source.getHostAddress());
				}
				// refresh entry
				neighbours.put(source, LocalTime.now().plusSeconds((HELLO_LOSS + 1) * HELLO_INTERVAL));
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	// RREQ

	private static boolean RREQreceivedBefore(JSONObject json) {
		String sourceIP = (String) json.get("sourceip");
		long broadcastID = (long) json.get("broadcastid");
		for (JSONObject current : receivedRREQList) {
			String currentsourceIP = (String) current.get("sourceip");
			long currentbroadcastID = (long) current.get("broadcastid");
			if (currentsourceIP.equals(sourceIP) && currentbroadcastID == broadcastID) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private static void receivedRREQ(JSONObject json, InetAddress neighbor) {
		try {
			// if (!neighbor.equals(InetAddress.getByName("192.168.5.3"))) {
			if (!RREQreceivedBefore(json)) {
				// remember json
				receivedRREQList.add(json);
				// extract data
				InetAddress sourceIP = InetAddress.getByName((String) json.get("sourceip"));
				InetAddress destIP = InetAddress.getByName((String) json.get("destip"));
				long hopCount = (long) json.get("hopcount");

				// from me?
				if (!myIP.equals(sourceIP)) {

					newLog("[RREQ] Received from: " + sourceIP.getHostAddress() + " -> " + destIP.getHostAddress());

					// for me?
					if (myIP.equals(destIP)) {
						// get public key
						PublicKey publicKey = RSA.stringToPublicKey((String) json.get("publickey"));
						// Create Sessionkey
						SecretKey sessionKey = AES.generateKey();
						// add Forwarding entry
						ForwardingTableService.addEntry(sourceIP, neighbor, hopCount, publicKey, sessionKey);

						// encrypt keys for reply
						String skeyString = AES.keyToString(sessionKey);
						String stepone = RSA.encrypt(skeyString, publicKey);
						String firsthalf = stepone.substring(0, stepone.length() / 2);
						String secondhalf = stepone.substring(stepone.length() / 2, stepone.length());
						String encryptedSessionKey = RSA.encrypt(firsthalf, RSA.getPrivateKey())
								+ RSA.encrypt(secondhalf, RSA.getPrivateKey());
						// send reply
						sendRREP(sourceIP, hopCount, encryptedSessionKey);
					} else {
						// add reverse entry
						ReverseTableService.addEntry(sourceIP, neighbor, hopCount);
						// increment hopcount
						json.put("hopcount", hopCount + 1);
						// rebroadcast RREQ
						sendMulticastJson(json);

					}
				}
				// }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void findRoute(InetAddress dest) {
		if (!ForwardingTableService.hasEntry(dest)) {
			// log entry
			newLog("[RREQ] Finding route to: " + dest.getHostAddress());
			// increment broadcastID
			broadcastID++;
			// get json
			JSONObject json = JSONservice.composeRREQ(myIP, broadcastID, dest, 0);
			// send
			sendMulticastJson(json);
		}
	}

	// RERR

	@SuppressWarnings("unchecked")
	public static void receivedRERR(JSONObject json) {
		try {
			if (!receivedRERRList.contains(json)) {
				receivedRERRList.add(json);

				InetAddress sourceIP = InetAddress.getByName((String) json.get("sourceip"));

				newLog("[RERR] Received from: " + sourceIP.getHostAddress());

				JSONArray array = (JSONArray) json.get("unreachable");
				String[] strings = (String[]) array.toArray(new String[0]);
				InetAddress[] unreachable = new InetAddress[array.size()];
				for (int i = 0; i < strings.length; i++) {
					unreachable[i] = InetAddress.getByName(strings[i]);
				}

				// update routing tables
				for (InetAddress i : unreachable) {
					ForwardingTableService.removeEntry(i);
				}

				// give further
				sendMulticastJson(json);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void sendRERR(InetAddress failedLink) {
		// log entry
		newLog("[RERR] Send");
		// get unreachable nodes
		InetAddress[] unreachable = ForwardingTableService.getNodes(failedLink);
		// remove from ftable
		for (InetAddress i : unreachable) {
			ForwardingTableService.removeEntry(i);
		}
		// send json
		JSONObject json = JSONservice.composeRERR(myIP, unreachable);
		sendMulticastJson(json);
	}

	// RREP

	@SuppressWarnings("unchecked")
	private static void receivedRREP(InetAddress neighbour, JSONObject json) {
		try {
			InetAddress sourceIP = InetAddress.getByName((String) json.get("sourceip"));
			InetAddress destIP = InetAddress.getByName((String) json.get("destip"));
			long hopCount = (long) json.get("hopcount");
			// log entry
			newLog("[RREP] Received: " + sourceIP.getHostAddress() + " -> " + destIP.getHostAddress());

			// get publikKey
			PublicKey publicKey = RSA.stringToPublicKey((String) json.get("publickey"));
			if (destIP.equals(myIP)) {
				// sessionKey
				String crypto = (String) json.get("sessionkey");
				String firsthalf = crypto.substring(0, crypto.length() / 2);
				String secondhalf = crypto.substring(crypto.length() / 2, crypto.length());
				String stepone = RSA.decrypt(firsthalf, publicKey) + RSA.decrypt(secondhalf, publicKey);
				String sKeyString = RSA.decrypt(stepone, RSA.getPrivateKey());
				SecretKey sessionKey = AES.stringToKey(sKeyString);
				// add forwarding entry with session Key
				ForwardingTableService.addEntry(sourceIP, neighbour, hopCount, publicKey, sessionKey);
			} else {
				// add forwarding to RREP source
				ForwardingTableService.addEntry(sourceIP, neighbour, hopCount, null, null);
				try {
					RTableEntry re = ReverseTableService.getEntry(destIP);
					// add forwarding to RREP dest
					ForwardingTableService.addEntry(destIP, re.nextHopAddress, re.hopsToSource, null, null);
					// increase hopcount
					json.put("hopcount", hopCount + 1);
					// send further
					sendUnicastJson(re.nextHopAddress, json);
				} catch (NoEntryException e) {
					e.printStackTrace();
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private static void sendRREP(InetAddress dest, long hopCount, String encryptedSessionKey) {
		// log entry
		newLog("[RREP] Send to: " + dest.getHostAddress());
		// get json
		JSONObject json = JSONservice.composeRREP(dest, myIP, hopCount, encryptedSessionKey);
		try {
			sendUnicastJson(ForwardingTableService.getEntry(dest).nextHopAddress, json);
		} catch (NoEntryException e) {
			e.printStackTrace();
		}
	}

	// DATA

	@SuppressWarnings("unchecked")
	private static void receivedDATA(JSONObject json) {
		try {
			InetAddress sourceIP = InetAddress.getByName((String) json.get("sourceip"));
			InetAddress destIP = InetAddress.getByName((String) json.get("destip"));

			newLog("[DATA] Received: " + sourceIP.getHostAddress() + " -> " + destIP.getHostAddress());

			// for me?
			if (destIP.equals(myIP)) {
				// send Ack
				sendACK(sourceIP, json);
				// decrypt message
				try {
					String crypto = (String) json.get("data");
					FTableEntry fe = ForwardingTableService.getEntry(sourceIP);
					json.put("data", AES.decrypt(crypto, fe.sessionkey));
				} catch (NoEntryException e) {
					e.printStackTrace();
				}
				// give to Application layer
				DataController.receivedMessage(json);
			} else {
				try {
					FTableEntry fe = ForwardingTableService.getEntry(destIP);
					// send according to forwarding table
					sendUnicastJson(fe.nextHopAddress, json);
				} catch (NoEntryException e) {
					// nothing
				}
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	public static void sendDATA(JSONObject json) {
		try {
			// get destination of data
			InetAddress destIP = InetAddress.getByName((String) json.get("destip"));
			// put in waiting
			waiting.put(json, LocalTime.now());
			// find route if neccessary
			if (!ForwardingTableService.hasEntry(destIP)) {
				// send routing request
				findRoute(destIP);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	// ACK

	private static void receivedACK(JSONObject json) {
		try {
			InetAddress destIP = InetAddress.getByName((String) json.get("destip"));
			if (myIP.equals(destIP)) {
				// get hash
				String hash = (String) json.get("data");
				// remove from needAck

				JSONObject[] temp = needAck.keySet().toArray(new JSONObject[0]);
				JSONObject toRemove = null;
				for (int i = 0; i < temp.length; i++) {
					JSONObject j = temp[i];
					String jHash = HashService.simpleHash(j.toJSONString());
					if (jHash.equals(hash)) {
						toRemove = j;
					}
				}
				// log entry
				if (toRemove != null) {
					newLog("[ACK] Acknowledged: " + (String) toRemove.get("data"));
					needAck.remove(toRemove);
				}
			} else {
				try {
					FTableEntry fe = ForwardingTableService.getEntry(destIP);
					sendUnicastJson(fe.nextHopAddress, json);
				} catch (NoEntryException e) {
					//
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void sendACK(InetAddress dest, JSONObject json) {
		newLog("[ACK] Acknowledging to: " + dest.getHostAddress());
		// get ack text
		String hash = HashService.simpleHash(json.toJSONString());
		// to json
		JSONObject ack = JSONservice.composeAck(myIP, dest, hash);
		// send according to ftable
		try {
			FTableEntry fe = ForwardingTableService.getEntry(dest);
			sendUnicastJson(fe.nextHopAddress, ack);
		} catch (NoEntryException e) {
			findRoute(dest);
			e.printStackTrace();
		}
	}

	public static void sendMulticastJson(JSONObject json) {
		String msg = json.toJSONString();
		Multicast.send(msg);
	}

	public static void sendUnicastJson(InetAddress dest, JSONObject json) {
		String msg = json.toJSONString();
		Unicast.send(dest, msg);
	}

	private static synchronized void receivedUnicast(Socket sock) {
		try {
			// read data
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String input = in.readLine();
			// get json
			JSONObject json = JSONservice.getJson(input);
			// get datatype
			long type = (long) json.get("type");
			// manage input
			switch ("" + type) {
			case "" + TYPE_DATA:
				receivedDATA(json);
				break;
			case "" + TYPE_RREP:
				receivedRREP(sock.getInetAddress(), json);
				break;
			case "" + TYPE_ACK:
				receivedACK(json);
				break;
			}
			// close socket
			in.close();
			sock.close();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static synchronized void receivedMulticast(DatagramPacket p) {
		try {
			// ignore own broadcasts
			if (!p.getAddress().equals(myIP)) {
				// get data
				byte[] buffer = p.getData();
				String data = new String(buffer);
				// get json
				JSONObject json = JSONservice.getJson(data);
				// get datatype
				long type = (long) json.get("type");
				// manage packet
				switch ("" + type) {
				case "" + TYPE_HELLO:
					receivedHELLO(json);
					break;
				case "" + TYPE_RREQ:
					receivedRREQ(json, p.getAddress());
					break;
				case "" + TYPE_RERR:
					receivedRERR(json);
					break;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg0 instanceof Multicast) {
			receivedMulticast((DatagramPacket) arg1);
		} else if (arg0 instanceof Unicast) {
			receivedUnicast((Socket) arg1);
		}
	}

	@SuppressWarnings("rawtypes")
	public static String getAddress(String s) {
		Enumeration e = null;
		try {
			e = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		while (e.hasMoreElements()) {
			NetworkInterface n = (NetworkInterface) e.nextElement();
			Enumeration ee = n.getInetAddresses();
			while (ee.hasMoreElements()) {
				InetAddress i = (InetAddress) ee.nextElement();
				String address = i.getHostAddress();
				if (address.startsWith(s)) {
					return address;
				}
			}
		}
		return null;
	}

	private class SayHello implements Runnable {

		@Override
		public void run() {
			// sending hello every second
			while (true) {
				JSONObject json = JSONservice.composeHello(myIP);
				// send
				sendMulticastJson(json);
				// wait
				try {
					Thread.sleep(HELLO_INTERVAL * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class CheckLists implements Runnable {
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			while (true) {
				try {
					// Neighbours
					for (InetAddress i : neighbours.keySet()) {
						LocalTime expire = neighbours.get(i);
						if (expire.isBefore(LocalTime.now())) {
							// Entry expired! send RERR
							sendRERR(i);
							// remove from list
							neighbours.remove(i);
							newLog("[HELLO] lost connection to: " + i.getHostAddress());
						}
					}
					// waiting
					for (JSONObject j : waiting.keySet()) {
						// get destination
						InetAddress destIP = InetAddress.getByName((String) j.get("destip"));
						// route has been found?
						if (ForwardingTableService.hasEntry(destIP)) {
							// get forwarding entry
							FTableEntry fe = ForwardingTableService.getEntry(destIP);
							// remove from waiting
							waiting.remove(j);
							// encrypt message
							String data = (String) j.get("data");
							j.put("data", AES.encrypt(data, fe.sessionkey));
							// add to needAck
							needAck.put(j, LocalTime.now());
							// send json
							sendUnicastJson(fe.nextHopAddress, j);
						} else {
							// get timestamp of json
							LocalTime timestamp = waiting.get(j);
							// timed out ?
							if (timestamp.plusSeconds(TIMEOUT_WAITING).isBefore(LocalTime.now())) {
								// renew timestamp
								waiting.put(j, LocalTime.now());
								// reinitiate routing
								findRoute(destIP);
							}
						}
					}
					// acknowledgements
					for (JSONObject j : needAck.keySet()) {
						// get timestamp of json
						LocalTime timestamp = needAck.get(j);
						// timed out ?
						if (timestamp.plusSeconds(TIMEOUT_ACK).isBefore(LocalTime.now())) {
							// renew timestamp
							needAck.put(j, LocalTime.now());
							// initiate sending again
							sendDATA(j);
						}
					}

				} catch (ConcurrentModificationException | NullPointerException e) {
					// nothing
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (NoEntryException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
