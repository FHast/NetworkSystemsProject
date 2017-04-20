package networkLayer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;

/**
 * This service is used to receive and send unicast packets.
 */
public class Unicast extends Observable implements Runnable {
	private static final int PORT = 4242;
	private static ServerSocket ssock;

	public void init() throws IOException {
		ssock = new ServerSocket(PORT);
		ssock.setReceiveBufferSize(50000);
	}

	/**
	 * Sends a packet to the given destination.
	 * 
	 * @param dest
	 *            The destination InetAddress
	 * @param msg
	 *            The packet as a String representation of the JSON object
	 */
	public static void send(InetAddress dest, String msg) {
		try {
			// create socket
			Socket sock = new Socket(dest, PORT);
			sock.setSendBufferSize(30000);
			// send data
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			out.println(msg);
			// close
			out.close();
			sock.close();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	/**
	 * Waiting for incoming packets.
	 */
	@Override
	public void run() {
		while (true) {
			try {
				// accept Connections
				Socket sock = ssock.accept();
				sock.setReceiveBufferSize(50000);
				// notify observers
				setChanged();
				notifyObservers(sock);
				clearChanged();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
