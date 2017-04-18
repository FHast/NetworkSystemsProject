package networkLayer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;

public class Unicast extends Observable implements Runnable {
	private static final int PORT = 4242;
	private static ServerSocket ssock;

	static {
		try {
			ssock = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void send(InetAddress dest, String msg) {
		try {
			// create socket
			Socket sock = new Socket(dest, PORT);
			// send data
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			out.println(msg);
			// close
			out.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				// accept Connections
				Socket sock = ssock.accept();
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
