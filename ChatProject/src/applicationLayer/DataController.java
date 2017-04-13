package applicationLayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;

import org.json.simple.JSONObject;

import controller.Controller;
import networkLayer.JSONservice;
import networkLayer.NetworkController;

public class DataController {

	public static final int DATA_TYPE_TEXT = 1;
	public static final int DATA_TYPE_IMAGE = 2;
	public static final int DATA_TYPE_ACK = 0;

	public DataController() {

		// start networking layer
		new NetworkController();

		Controller.mainWindow.log("[Thread] Application Layer started.");
	}

	public static InetAddress getMyIP() {
		return NetworkController.myIP;
	}

	public static void sendMessage(InetAddress destIP, String message) {
		newLog("[DATA] Trying to send: " + message);

		// data to send
		JSONObject json = JSONservice.composeDataText(getMyIP(), destIP, message);
		// send data
		sendData(json);
	}

	public static void sendFile(InetAddress destIP, File file) {
		newLog("[DATA] Trying to send: " + file.getName() + " / " + file.getPath());

		// get data
		String url = file.getPath();
		String appendix = FileService.getAppendix(url);
		String data = FileService.fileToString(url);
		// get json
		JSONObject json = JSONservice.composeDataFile(getMyIP(), destIP, data, appendix);
		// send data
		sendData(json);

	}

	private static void sendData(JSONObject json) {
		NetworkController.sendDATA(json);
	}

	// COMPOSITION

	public static void receivedMessage(JSONObject json) {
		int device = Integer.parseInt(((String) json.get("sourceip")).split("[.]")[3]);
		if (((String) json.get("datatype")).equals("" + DATA_TYPE_TEXT)) {
			// send to controller
			Controller.receivedMessage(device, (String) json.get("data"));
		} else {
			try {
				String appendix = (String) json.get("datatype");
				String data = (String) json.get("data");
				// get file
				String path = FileService.stringToFile(data, appendix);

				newLog("[DATA] Received file (" + appendix + ") created: " + path);

				// send to controller
				Controller.receivedFile(device, path);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public static void newLog(String s) {
		Controller.mainWindow.log(s);
	}
}
