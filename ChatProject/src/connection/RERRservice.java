package connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import connection.tables.FTableEntry;
import connection.tables.ForwardingTableService;
import connection.tables.NoEntryException;

public class RERRservice implements Runnable {

	public static final int RERR_ID = 153;
	public static final int RERR_PORT = 2004;
	public static final String RERR_TRAFFIC_ADDRESS = "128.0.0.3";

	private static InetAddress group;
	private static InetAddress myIP = Controller.myIP;
	private static MulticastSocket msock;

	protected static ArrayList<JSONObject> rcvdRerrs = new ArrayList<>();

	static {
		try {
			group = InetAddress.getByName(RERR_TRAFFIC_ADDRESS);
			msock = new MulticastSocket(RERR_PORT);
			msock.joinGroup(group);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendRERR(InetAddress errorlink) {
		Controller.mainWindow.log("[RERR] Notifying about link failure.");
		
		InetAddress[] unreachable  = ForwardingTableService.getNodes(errorlink);
		JSONObject rerr = JSONservice.composeRERR(myIP, Controller.mySeq(), unreachable);
		String msg = rerr.toJSONString();
		byte[] buf = msg.getBytes();
		DatagramPacket p = new DatagramPacket(buf, buf.length, group, RERR_PORT);
		try {
			msock.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				// accept cconnection
				byte[] buffer = new byte[10000];
				DatagramPacket p = new DatagramPacket(buffer, buffer.length);
				msock.receive(p);

				// extract Data
				String msg = new String(p.getData());
				Controller.mainWindow.log("[RREQ] Received " + msg);
				JSONObject json = JSONservice.getJson(msg);

				if (!rcvdRerrs.contains(json)) {

					rcvdRerrs.add(json);

					InetAddress sourceIP = InetAddress.getByName((String) json.get("sourceip"));
					long sourceSeq = (long) json.get("sourceseq");
					InetAddress[] unreachable = (InetAddress[]) json.get("unreachable");

					// update source seq
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
					msock.send(p);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
}
