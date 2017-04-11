package networkLayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Observable;

public class RERRservice extends Observable implements Runnable {
	
	public static final int RERR_PORT = 2004;
	public static final String RERR_TRAFFIC_ADDRESS = "228.0.0.3";

	private static InetAddress group;
	private static MulticastSocket msock;

	static {
		try {
			group = InetAddress.getByName(RERR_TRAFFIC_ADDRESS);
			msock = new MulticastSocket(RERR_PORT);
			msock.joinGroup(group);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendRERR(String message) {
		byte[] buf = message.getBytes();
		DatagramPacket p = new DatagramPacket(buf, buf.length, group, RERR_PORT);
		try {
			msock.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void rebroadcast(DatagramPacket p) throws IOException {
		msock.send(p);
	}

	@Override
	public void run() {
		while (true) {
			try {
				// accept cconnection
				byte[] buffer = new byte[10000];
				DatagramPacket p = new DatagramPacket(buffer, buffer.length);
				msock.receive(p);
				
				// give to observers
				setChanged();
				notifyObservers(p);
				clearChanged();
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
}
