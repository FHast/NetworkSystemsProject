package connection;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import connection.tables.ForwardingTableService;
import connection.tables.ReverseTableService;
import gui.MainWindow;
import gui.StartPopup;

public class Controller {

	private static long mySeq = 0;

	public static InetAddress myIP;
	static {
		try {
			myIP = InetAddress.getByName(getAddress("192.168.5."));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private boolean shutdown = false;

	public static MainWindow mainWindow;

	public Controller() {

		mainWindow = new MainWindow(this);
		mainWindow.setVisible(true);
		
		if (myIP.toString().equals("localhost/127.0.0.1")){
			new StartPopup("Not connected","You are currently not connected","to the Ad-Hoc network!").setVisible(true);
		}

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

		Controller.mainWindow.log("MY IP: " + myIP);

		while (!shutdown) {

		}
	}

	public static synchronized void incrementSeq() {
		Controller.mainWindow.log("[Controller] Incrementing sequence number");
		mySeq++;
	}

	public static long mySeq() {
		return mySeq;
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

	public static String getAddress(String firstPart) {
		Enumeration e = null;
		try {
			e = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException exc) {

		}
		while (e.hasMoreElements()) {
			NetworkInterface n = (NetworkInterface) e.nextElement();
			Enumeration ee = n.getInetAddresses();
			while (ee.hasMoreElements()) {
				InetAddress i = (InetAddress) ee.nextElement();
				String address = i.getHostAddress();
				if (address.startsWith(firstPart)) {
					return address;
				}
			}
		}
		return null;
	}

	public static void main(String[] args) {
		Controller c = new Controller();
	}
}
