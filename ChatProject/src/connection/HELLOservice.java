package connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalTime;
import java.util.HashMap;

public class HELLOservice implements Runnable {
	protected static HashMap<InetAddress, Long> neighbours = new HashMap<>();
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
		System.out.println("[Thread] Start hello protocol execution.");
		Thread checker = new Thread(new mapchecker());
		checker.start();
		
		Thread listener = new Thread(new listener());
		listener.start();

		while (true) {
			String msg = "Hello from the otter slide";
			byte[] b = msg.getBytes();
			DatagramPacket p = new DatagramPacket(b, b.length, group, HELLO_PORT);
			try {
				msock.send(p);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(HELLO_INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static class listener implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
				// receive hellos
				byte[] buffer = new byte[50];
				DatagramPacket p = new DatagramPacket(buffer, buffer.length);
				msock.receive(p);
				// add to list
				InetAddress source = p.getAddress();
				neighbours.put(source, System.nanoTime());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private static class mapchecker implements Runnable {

		@Override
		public void run() {
			while (true) {
				for (InetAddress i : neighbours.keySet()) {
					long then = neighbours.get(i);
					long thenPlusTimeout = then + ((HELLO_LOSS + 1) * HELLO_INTERVAL) * 1000000;
					if (System.nanoTime() > thenPlusTimeout) {
						// Entry expired! send RERR
						
						// increase my sequence number
						Controller.incrementSeq();
						
						RERRservice.sendRERR(i);
					}
				}
			}
		}
	}
}
