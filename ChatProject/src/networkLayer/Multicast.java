package networkLayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Observable;

/**
 * The service from which multicast messages can be sent, e.g. routing requests. These will be forwarded to all immediate
 * neighbours.
 */
public class Multicast extends Observable implements Runnable {
	private static final String ADDRESS = "228.0.0.1";
	private static final int PORT = 1337;
	private static InetAddress group;
	private static MulticastSocket msock;
	
	static {
		try {
			group = InetAddress.getByName(ADDRESS);
			msock = new MulticastSocket(1337);
			msock.joinGroup(group);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Broadcasts a message to all its neighbours.
	 * @param msg The message to be sent.
	 */
	public static void send(String msg) {
		byte[] buffer = msg.getBytes();
		DatagramPacket p = new DatagramPacket(buffer, buffer.length, group, PORT);
		try {
			msock.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method checks for incoming broadcast messages.
	 */
	@Override
	public void run() {
		while (true) {
			while (true) {
				// receive packet
				byte[] buffer = new byte[10000];
				DatagramPacket p = new DatagramPacket(buffer, buffer.length);
				try {
					msock.receive(p);
					
					// notify observers
					setChanged();
					notifyObservers(p);
					clearChanged();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
