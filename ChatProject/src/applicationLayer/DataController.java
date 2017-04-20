package applicationLayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

import org.json.simple.JSONObject;

import controller.Controller;
import networkLayer.HashService;
import networkLayer.JSONservice;
import networkLayer.NetworkController;

/**
 * Controller for the Application layer that sends either text messages or files. Takes care of
 * splitting up data if it is too big and putting it back together.
 */
public class DataController {

	public static final int DATA_TYPE_TEXT = 1;
	private static final int MAX_DATA_BYTES = 45000;

	private static HashMap<String, ArrayList<JSONObject>> receivedFragments = new HashMap<>();

	/**
	 * Creates the DataController which starts the NetworkController, and
	 * starts the Thread to remove received, outdated fragments.
	 * @throws IOException 
	 */
	public DataController() throws IOException {

		// start networking layer
		new NetworkController();

		// start thread that removes the received, outdated fragments
		new Thread(new removeFragments()).start();

		// add log message
		newLog("[Thread] Application Layer started.");
	}

	/**
	 * Requests the own IP from the NetworkController and returns it.
	 * @return The own IP as an InetAddress
	 */
	public static InetAddress getMyIP() {
		return NetworkController.myIP;
	}

	/**
	 * Sends a message to the given address.
	 * @param destIP The IP to which the message should be sent
	 * @param message The raw message as a String
	 */
	public static void sendMessage(InetAddress destIP, String message) {
		// add log message
		newLog("[DATA] Trying to send: " + message);

		// data to send
		JSONObject json = JSONservice.composeDataText(getMyIP(), destIP, message);
		
		// send data
		sendData(json);
	}

	/**
	 * Sends any file to the given address.
	 * @param destIP The given address
	 * @param file The file
	 */
	public static void sendFile(InetAddress destIP, File file) {
		newLog("[DATA] Trying to send: " + file.getName() + " / " + file.getPath());

		// get data
		String url = file.getPath();
		String appendix = FileService.getAppendix(url);
		String data = FileService.fileToString(url);
		// get json
		JSONObject[] fragments = fragmentate(destIP, appendix, data);
		// send data
		for (JSONObject j : fragments) {
			sendData(j);
		}
	}

	/**
	 * Splits up the given file into multiple, ready to be sent packets in the form of JSONObjects (if necessary).
	 * The maximum segment size is given by the constant MAX_DATA_BYTES.
	 * @param destIP The address to which the file should be sent to
	 * @param appendix The file extension
	 * @param data The base64 encoded file content
	 * @return
	 */
	private static JSONObject[] fragmentate(InetAddress destIP, String appendix, String data) {
		ArrayList<JSONObject> fragments = new ArrayList<>();
		// compute total frag count
		int fragtotal = (int) Math.ceil(data.length() / (double) MAX_DATA_BYTES);
		// compute hash
		String filehash = HashService.simpleHash(data + LocalTime.now());

		// get fragments
		int i = 0;
		while (data.length() > MAX_DATA_BYTES) {
			// get fragment data
			String fragment = data.substring(0, MAX_DATA_BYTES);
			// compose json
			JSONObject json = JSONservice.composeDataFile(getMyIP(), destIP, fragment, appendix, i, fragtotal,
					filehash);
			// add to result list
			fragments.add(json);
			// alter data string
			data = data.substring(MAX_DATA_BYTES);
			// increase number
			i++;
		}
		// get last fragment
		JSONObject json = JSONservice.composeDataFile(getMyIP(), destIP, data, appendix, i, fragtotal, filehash);
		// add to list
		fragments.add(json);
		return fragments.toArray(new JSONObject[0]);
	}

	/**
	 * Sends a packet in the form of an JSONObject.
	 * @param json The packet
	 */
	private static void sendData(JSONObject json) {
		NetworkController.sendDATA(json);
	}

	// COMPOSITION

	/**
	 * This method is called when a packet has been received.
	 * @param json The packet
	 */
	public static void receivedMessage(JSONObject json) {
		// get device (last digit of the IP) + timestamp
		int device = Integer.parseInt(((String) json.get("sourceip")).split("[.]")[3]);
		LocalTime sendAt = LocalTime.parse((String) json.get("timestamp"));
		
		// if it's text, pass the data to the Controller
		if (((String) json.get("datatype")).equals("" + DATA_TYPE_TEXT)) {
			// send to controller
			Controller.receivedMessage(device, (String) json.get("data"), sendAt);
		} else {
			// get filehash
			String filehash = (String) json.get("filehash");

			// put into hashMap
			if (!receivedFragments.containsKey(filehash)) {
				receivedFragments.put(filehash, new ArrayList<JSONObject>());
			}
			
			// check if the fragment is new and the file is complete now
			long fragnumber = (long) json.get("fragnumber");
			if (isNewFragment(filehash, fragnumber)) {
				receivedFragments.get(filehash).add(json);
				// check if complete
				long fragtotal = (long) json.get("fragtotal");
				newLog("[DATA] New Fragment: (" + receivedFragments.get(filehash).size() + "/" + fragtotal + ")");

				if (receivedFragments.get(filehash).size() == fragtotal) {
					combineFragments(filehash, fragtotal);
				}
			}
		}
	}

	/**
	 * Checks whether the received fragment has not been received before by checking the file hash and the fragnumber
	 * @param filehash The hash which uniquely identifies the file
	 * @param fragnumber The sequence number of the fragment
	 * @return Whether it is new
	 */
	private static boolean isNewFragment(String filehash, long fragnumber) {
		// search for the fragment number in the list of fragment numbers corresponding the the file hash
		for (JSONObject j : receivedFragments.get(filehash)) {
			long currentNumber = (long) j.get("fragnumber");
			if (currentNumber == fragnumber) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Combines received fragments and saves the file.
	 * @param filehash The unique identifier of the file
	 * @param fragtotal Total amount of fragments
	 */
	private static void combineFragments(String filehash, long fragtotal) {
		ArrayList<JSONObject> fragments = receivedFragments.get(filehash);
		// get total data string
		String data = "";
		for (int i = 0; i < fragtotal; i++) {
			for (JSONObject j : fragments) {
				long fragnumber = (long) j.get("fragnumber");
				if (fragnumber == i) {
					data += (String) j.get("data");
				}
			}
		}
		// get appendix
		String appendix = (String) fragments.get(0).get("datatype");
		int device = Integer.parseInt(((String) fragments.get(0).get("sourceip")).split("[.]")[3]);
		LocalTime sendAt = LocalTime.parse((String) fragments.get(0).get("timestamp"));
		try {
			// get file
			String path = FileService.stringToFile(data, appendix);

			newLog("[DATA] Received file (" + appendix + ") created: " + path);

			// send to controller
			Controller.receivedFile(device, path, sendAt);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Runnable that checks whether the received fragments are out of date. If so, they are removed. 
	 */
	private class removeFragments implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					// go through the keySet of the received fragments HashMap (i.e., the file hashes)
					for (String s : receivedFragments.keySet()) {
						ArrayList<JSONObject> list = receivedFragments.get(s);
						LocalTime time = LocalTime.parse((String) list.get(0).get("timestamp"));
						if (time.plusMinutes(15).isBefore(LocalTime.now())) {
							receivedFragments.remove(s);
						}
					}
				} catch (ConcurrentModificationException | IndexOutOfBoundsException ex) {

				}
			}
		}
	}

	/**
	 * Helper function for accessing the log method (also possible: Controller.mainWindow.log(String).
	 * @param s
	 */
	public static void newLog(String s) {
		Controller.mainWindow.log(s);
	}
}
