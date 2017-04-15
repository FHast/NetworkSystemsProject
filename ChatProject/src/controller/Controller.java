package controller;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalTime;

import applicationLayer.DataController;
import applicationLayer.FileService;
import gui.MainWindow;
import gui.Message;
import networkLayer.NetworkController;

public class Controller {

	public static MainWindow mainWindow;

	public Controller() {		
		// view
		mainWindow = new MainWindow(this);
		mainWindow.setVisible(true);

		// Start application layer 
		new DataController();

		Controller.mainWindow.log("MY IP: " + getMyIP().getHostAddress());
		
		receivedMessage(3, "5", LocalTime.now().minusSeconds(3));
		receivedMessage(3, "2", LocalTime.now().minusSeconds(30));
		receivedMessage(3, "3", LocalTime.now().minusSeconds(10));
		receivedMessage(3, "4", LocalTime.now().minusSeconds(5));
		receivedMessage(3, "1", LocalTime.now().minusSeconds(80));
		receivedMessage(4, "8", LocalTime.now().minusSeconds(7));
		
		while (true) {

		}
	}
	
	public static InetAddress getMyIP() {
		return NetworkController.myIP;
	}
	
	public static void receivedMessage(int device, String s, LocalTime sendTime) {
		Controller.mainWindow.addMessage(device, s,  Message.TYPE_TEXT, sendTime);
	}

	public static void receivedFile(int device, String path, LocalTime sendTime) {
		System.out.println(path);
		String a = FileService.getAppendix(path);
		if (a.equals("gif") || a.equals("png") || a.equals("jpeg") || a.equals("jpg")) {
			Controller.mainWindow.addMessage(device, path, Message.TYPE_IMAGE, sendTime);
		} else {
			Controller.mainWindow.addMessage(device, path, Message.TYPE_FILE, sendTime);
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
