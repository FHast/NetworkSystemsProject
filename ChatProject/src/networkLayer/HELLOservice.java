package networkLayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Observable;

public class HELLOservice extends Observable implements Runnable {
	private static final int HELLO_INTERVAL = 1000;
	private static final int HELLO_LOSS = 2;
	private static final int HELLO_PORT = 2003;
	private static final String HELLO_TRAFFIC_ADDRESS = "128.0.0.2";

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
				Thread.sleep(HELLO_INTERVAL);
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
				HashMap<InetAddress, Long> neighbours = NetworkController.getNeighbours();
				try {
					for (InetAddress i : neighbours.keySet()) {
						long then = neighbours.get(i);
						long thenPlusTimeout = then + ((HELLO_LOSS + 1) * HELLO_INTERVAL) * 1000000;
						if (System.nanoTime() > thenPlusTimeout) {
							// increase my sequence number
							NetworkController.incrementSeq();
							// Entry expired! send RERR
							NetworkController.sendRERR(i);
							// remove from list
							neighbours.remove(i);
						}
					}
				} catch (ConcurrentModificationException e) {
					// nothing
				}
			}
		}
	}
}
