package controller;

import java.net.InetAddress;
import java.net.UnknownHostException;

import applicationLayer.DataController;
import gui.MainWindow;
import networkLayer.NetworkController;

public class Controller {

	private boolean shutdown = false;

	public static MainWindow mainWindow;

	public Controller() {		
		// view
		mainWindow = new MainWindow(this);
		mainWindow.setVisible(true);

		// Start application layer 
		new DataController();

		Controller.mainWindow.log("MY IP: " + getMyIP().getHostAddress());

		while (!shutdown) {

		}
	}
	
	public static InetAddress getMyIP() {
		return NetworkController.getMyIP();
	}
	
	public static void receivedMessage(int device, String s) {
		Controller.mainWindow.addMessage(device, s);
		Controller.mainWindow.refreshContactList();
	}

	public void sendMessage(int device, String message) {
		InetAddress destIP;
		try {
			destIP = InetAddress.getByName("192.168.5." + device);
			DataController.sendMessage(destIP, message);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Controller();
	}
}
