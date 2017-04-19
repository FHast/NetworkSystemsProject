package applicationLayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONObject;

import controller.Controller;
import networkLayer.HashService;
import networkLayer.JSONservice;
import networkLayer.NetworkController;

public class DataController {

	public static final int DATA_TYPE_TEXT = 1;
	private static final int MAX_DATA_BYTES = 45000;

	private static HashMap<String, ArrayList<JSONObject>> receivedFragments = new HashMap<>();

	public DataController() {

		// start networking layer
		new NetworkController();

		newLog("[Thread] Application Layer started.");
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
		JSONObject[] fragments = fragmentate(destIP, appendix, data);
		// send data
		for (JSONObject j : fragments) {
			sendData(j);
		}
	}

	private static JSONObject[] fragmentate(InetAddress destIP, String appendix, String data) {
		ArrayList<JSONObject> fragments = new ArrayList<>();
		// compute total frag count
		int fragtotal = (int) Math.ceil(data.length() / (double) MAX_DATA_BYTES);
		// compute hash
		String filehash = HashService.simpleHash(data);

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

	private static void sendData(JSONObject json) {
		NetworkController.sendDATA(json);
	}

	// COMPOSITION

	public static void receivedMessage(JSONObject json) {
		int device = Integer.parseInt(((String) json.get("sourceip")).split("[.]")[3]);
		LocalTime sendAt = LocalTime.parse((String) json.get("timestamp"));
		if (((String) json.get("datatype")).equals("" + DATA_TYPE_TEXT)) {
			// send to controller7
			Controller.receivedMessage(device, (String) json.get("data"), sendAt);
		} else {
			// get filehash
			String filehash = (String) json.get("filehash");

			// put into hashMap
			if (!receivedFragments.containsKey(filehash)) {
				receivedFragments.put(filehash, new ArrayList<JSONObject>());
			}
			if (!receivedFragments.get(filehash).contains(json)) {
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
		receivedFragments.remove(filehash);
	}

	public static void newLog(String s) {
		Controller.mainWindow.log(s);
	}
}
