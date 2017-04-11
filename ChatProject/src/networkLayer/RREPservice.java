package networkLayer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;

import gui.StartPopup;

public class RREPservice extends Observable implements Runnable {
	private static boolean shutdown = false;
	// RREP
	public static final int RREP_TRAFFIC_PORT = 2002;

	private static ServerSocket ssock;

	static {
		try {
			ssock = new ServerSocket(RREP_TRAFFIC_PORT);
		} catch (IOException e) {
			new StartPopup("Application already running", "Another instance of this application is ",
					"already running. Please close it first!").setVisible(true);
		}

	}

	public static void sendRREP(InetAddress destIP, String msg) {
		try {
			// open connection
			Socket sock = new Socket(destIP.getHostAddress(), RREP_TRAFFIC_PORT);
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			// send data
			out.println(msg);
			// close connection
			out.close();
			sock.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			// check incoming
			while (!shutdown) {
				// accept connection
				Socket sock = ssock.accept();
				// notify observers
				setChanged();
				notifyObservers(sock);
				clearChanged();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			// do nothing, ssock wasnt created...
		}
	}

	public void shutdown() {
		shutdown = true;
	}
}
