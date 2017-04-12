package controller;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import applicationLayer.DataController;
import applicationLayer.FileService;
import gui.MainWindow;
import gui.Message;
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
		Controller.mainWindow.addMessage(device, s,  Message.TYPE_TEXT);
		Controller.mainWindow.refreshContactList();
	}

	public static void receivedFile(int device, String path) {
		System.out.println(path);
		String a = FileService.getAppendix(path);
		if (a.equals("gif") || a.equals("png") || a.equals("jpeg") || a.equals("jpg")) {
			Controller.mainWindow.addMessage(device, path, Message.TYPE_IMAGE);
		} else {
			Controller.mainWindow.addMessage(device, path, Message.TYPE_FILE);
		}
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
	
	public void sendFile(int device, File file) {
		InetAddress destIP;
		try {
			destIP = InetAddress.getByName("192.168.5." + device);
			DataController.sendFile(destIP, file);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Controller();
	}
}
