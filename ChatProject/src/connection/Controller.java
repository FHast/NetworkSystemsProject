package connection;

import java.net.InetAddress;
import java.net.UnknownHostException;

import connection.tables.ForwardingTableService;
import connection.tables.ReverseTableService;

public class Controller {
	
	public static InetAddress myIP;
	static {
		try {
			myIP = InetAddress.getByName("192.168.5.1");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	
	private boolean shutdown = false;

	public Controller() {
		// start threads
		Thread rreq = new Thread(new RREQservice());
		rreq.start();
		
		Thread rrep = new Thread(new RREPservice());
		rrep.start();
		
		Thread data = new Thread(new DATAservice());
		data.start();
		
		Thread ftable = new Thread(new ForwardingTableService());
		ftable.start();
		
		Thread rtable = new Thread(new ReverseTableService());
		rtable.start();
		
		sendMessage(2, "test message");
		
		while (!shutdown) {
			
		}
	}
	
	public void sendMessage(int device, String message) {
		InetAddress destIP;
		try {
			destIP = InetAddress.getByName("192.168.5." + device);
			DATAservice.sendText(destIP, message);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Controller c = new Controller();
	}
}
