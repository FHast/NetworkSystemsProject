package networkLayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalTime;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Observable;

public class HELLOservice extends Observable implements Runnable {
	public static final int HELLO_INTERVAL = 1;
	public static final int HELLO_LOSS = 3;
	private static final int HELLO_PORT = 2003;
	private static final String HELLO_TRAFFIC_ADDRESS = "228.0.0.2";

	protected static InetAddress group;
	protected static MulticastSocket msock;

	static {
		try {
			group = InetAddress.getByName(HELLO_TRAFFIC_ADDRESS);
			msock = new MulticastSocket(HELLO_PORT);
			msock.joinGroup(group);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		// start Threads
		Thread checker = new Thread(new mapchecker());
		checker.start();

		Thread listener = new Thread(new listener());
		listener.start();

		// sending hello every second
		while (true) {
			String msg = "Hello from the otter slide";
			// compose packet
			byte[] b = msg.getBytes();
			DatagramPacket p = new DatagramPacket(b, b.length, group, HELLO_PORT);
			// send
			try {
				msock.send(p);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// wait
			try {
				Thread.sleep(HELLO_INTERVAL*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private class listener implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					// receive hellos
					byte[] buffer = new byte[50];
					DatagramPacket p = new DatagramPacket(buffer, buffer.length);
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

	private class mapchecker implements Runnable {

		@Override
		public void run() {
			while (true) {
				HashMap<InetAddress, LocalTime> neighbours = NetworkController.getNeighbours();
				try {
					for (InetAddress i : neighbours.keySet()) {
						LocalTime expire = neighbours.get(i);
						if (expire.isBefore(LocalTime.now())) {
							// increase my sequence number
							NetworkController.incrementSeq();
							// Entry expired! send RERR
							NetworkController.sendRERR(i);
							// remove from list
							neighbours.remove(i);
							System.out.println("lost neighbour: " + i.getHostAddress());
						}
					}
					Thread.sleep(100);
				} catch (ConcurrentModificationException | InterruptedException e) {
					// nothing
				}
			}
		}
	}
}
