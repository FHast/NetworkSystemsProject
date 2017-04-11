package networkLayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Observable;

public class RREQservice extends Observable implements Runnable {
	// RREQ
	public static final String RREQ_TRAFFIC_ADDRESS = "228.0.0.1";
	public static final int RREQ_TRAFFIC_PORT = 2001;
	// Socket
	private static MulticastSocket msock;
	private static InetAddress group;

	static {
		try {
			group = InetAddress.getByName(RREQ_TRAFFIC_ADDRESS);
			msock = new MulticastSocket(RREQ_TRAFFIC_PORT);
			msock.joinGroup(group);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendRREQ(String msg) {
		try {
			DatagramPacket d = new DatagramPacket(msg.getBytes(), msg.length(), group, RREQ_TRAFFIC_PORT);
			msock.send(d);
			
			System.out.println("RREQ send");
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			// listen for RREQs
			while (true) {
				// receive packet
				byte[] buffer = new byte[250];
				DatagramPacket p = new DatagramPacket(buffer, buffer.length);
				msock.receive(p);
				
				System.out.println("RREQ received");

				// notify observers
				setChanged();
				notifyObservers(p);
				clearChanged();

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
