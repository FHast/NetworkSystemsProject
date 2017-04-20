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
import security.RSA;

/**
 * Main controller that controls all other controllers. Also contains the main method.
 */
public class Controller {

	public static MainWindow mainWindow;

	/**
	 * Generated key pair for encryption, starts the DataController and starts the main window.
	 */
	public Controller() {
		// generating key pairs
		if (!RSA.isKeyPairGenerated()) {
			RSA.generateKeyPair();
		}
		// view
		mainWindow = new MainWindow(this);
		mainWindow.setVisible(true);

		// Start application layer 
		new DataController();

		Controller.mainWindow.log("MY IP: " + getMyIP().getHostAddress());
		
		while (true) {

		}
	}
	
	/**
	 * Returns own IP as an InetAddress
	 * @return Own IP
	 */
	public static InetAddress getMyIP() {
		return NetworkController.myIP;
	}
	
	/**
	 * This method is called when a message has been received.
	 * @param device The last digit of the IP
	 * @param s The message
	 * @param sendTime The time the message was sent
	 */
	public static void receivedMessage(int device, String s, LocalTime sendTime) {
		Controller.mainWindow.addMessage(device, s,  Message.TYPE_TEXT, sendTime);
	}

	/**
	 * This method is called when a file has been received.
	 * @param device The last digit of the IP
	 * @param path The path to the file
	 * @param sendTime The time the message was sent
	 */
	public static void receivedFile(int device, String path, LocalTime sendTime) {
		String a = FileService.getAppendix(path);
		// we only accept gif, png, and jpg as an image, all others will be stores as a normal file
		if (a.equals("gif") || a.equals("png") || a.equals("jpeg") || a.equals("jpg")) {
			Controller.mainWindow.addMessage(device, path, Message.TYPE_IMAGE, sendTime);
		} else {
			Controller.mainWindow.addMessage(device, path, Message.TYPE_FILE, sendTime);
		}
	}
	
	/**
	 * Sends a message to the given device.
	 * @param device The last digit of the IP
	 * @param message The message
	 */
	public void sendMessage(int device, String message) {
		InetAddress destIP;
		try {
			destIP = InetAddress.getByName("192.168.5." + device);
			DataController.sendMessage(destIP, message);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a file to the given device.
	 * @param device The last digit of the IP
	 * @param file The file object
	 */
	public void sendFile(int device, File file) {
		InetAddress destIP;
		try {
			destIP = InetAddress.getByName("192.168.5." + device);
			DataController.sendFile(destIP, file);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The main method that starts the main controller.
	 * @param args
	 */
	public static void main(String[] args) {
		new Controller();
	}
}
