package networkLayer;

import java.net.InetAddress;
import java.time.LocalTime;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import applicationLayer.DataController;
import security.RSA;

/**
 * This service is used to compose packets and additionally, it provides basic JSON functionalities,
 * e.g. parsing a String into a JSON object and vice-versa.
 */
public class JSONservice {

	/**
	 * Parse a String that represents an JSONObject and construct that object.
	 * @param s The String to be parsed
	 * @return The constructed JSON object
	 * @throws ParseException Not a valid String
	 */
	public static JSONObject getJson(String s) throws ParseException {
		// format string into jsonarray
		String formatted = "";
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if ((int) c != 0) {
				formatted += c;
			}
		}

		JSONParser parser = new JSONParser();
		Object obj = parser.parse(formatted);
		JSONObject json = (JSONObject) obj;
		return json;
	}

	/**
	 * Composes a RREP packet as a JSON object
	 * @param dest The destination InetAddress
	 * @param source The source InetAddress
	 * @param hopcount The hopcount
	 * @param sessionKey The base64 encoded session key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject composeRREP(InetAddress dest, InetAddress source, long hopcount, String sessionKey) {
		JSONObject rrep = new JSONObject();
		rrep.put("type", NetworkController.TYPE_RREP);
		rrep.put("destip", dest.getHostAddress());
		rrep.put("sourceip", source.getHostAddress());
		rrep.put("hopcount", hopcount);
		rrep.put("publickey", RSA.publicKeyToString(RSA.getPublicKey()));
		rrep.put("sessionkey", sessionKey);
		return rrep;
	}

	/**
	 * Composes a RREQ packet as a JSON object
	 * @param sourceip The source InetAddress
	 * @param broadcastid The broadcast ID
	 * @param dest The destination InetAddress
	 * @param hopcount The hopcount
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject composeRREQ(InetAddress sourceip, long broadcastid, InetAddress dest, long hopcount) {
		JSONObject rreq = new JSONObject();
		rreq.put("type", (int) NetworkController.TYPE_RREQ);
		rreq.put("sourceip", sourceip.getHostAddress());
		rreq.put("broadcastid", broadcastid);
		rreq.put("destip", dest.getHostAddress());
		rreq.put("hopcount", hopcount);
		rreq.put("publickey", RSA.publicKeyToString(RSA.getPublicKey()));
		return rreq;
	}

	/**
	 * Composes a data packet of type TEXT and returns it as a JSON object
	 * @param sourceIP The source InetAddress
	 * @param destIP The destination InetAddress
	 * @param message The message text
	 * @return
	 */
	public static JSONObject composeDataText(InetAddress sourceIP, InetAddress destIP, String message) {
		return composeData(sourceIP, destIP, message, "" + DataController.DATA_TYPE_TEXT, NetworkController.TYPE_DATA, 0, 0, "");
	}

	/**
	 * Composes an ACK packet and returns it as a JSON object
	 * @param sourceIP The source InetAddress 
	 * @param destIP The destination InetAddress
	 * @param message The message text
	 * @return
	 */
	public static JSONObject composeAck(InetAddress sourceIP, InetAddress destIP, String message) {
		return composeData(sourceIP, destIP, message, "", NetworkController.TYPE_ACK, 0, 0, "");
	}

	/**
	 * Composes a data packet which contains a file and returns it as a JSON object.
	 * @param sourceIP The source InetAddress
	 * @param destIP The destination InetAddress
	 * @param data The base64 encoded data
	 * @param appendix The file extension
	 * @param fragnumber The current fragment sequence number
	 * @param fragtotal The total amount of fragments of this file
	 * @param filehash The hash of the file as an identifier of the file
	 * @return
	 */
	public static JSONObject composeDataFile(InetAddress sourceIP, InetAddress destIP, String data, String appendix, int fragnumber, int fragtotal, String filehash) {
		return composeData(sourceIP, destIP, data, appendix, NetworkController.TYPE_DATA, fragnumber, fragtotal, filehash);
	}

	/**
	 * Composes a RERR packet and returns it as a JSON object.
	 * @param sourceIP The source InetAddress
	 * @param unreachable The InetAddress that are not reachable anymore
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject composeRERR(InetAddress sourceIP, InetAddress[] unreachable) {

		JSONArray list = new JSONArray();
		for (InetAddress i : unreachable) {
			list.add(i.getHostAddress());
		}

		JSONObject rerr = new JSONObject();
		rerr.put("type", NetworkController.TYPE_RERR);
		rerr.put("sourceip", sourceIP.getHostAddress());
		rerr.put("unreachable", list);
		return rerr;
	}

	/**
	 * Composes a data packet of the given type and returns it as a JSON object
	 * @param sourceIP The source InetAddress
	 * @param destIP The destination InetAddress
	 * @param message The message text/data
	 * @param datatype Type of data
	 * @param type The message type
	 * @param fragnumber The current fragment sequence number
	 * @param fragtotal The total amount of fragments belonging to this file
	 * @param filehash The hash of the file which serves as an unique identifier
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static JSONObject composeData(InetAddress sourceIP, InetAddress destIP, String message, String datatype, int type, int fragnumber, int fragtotal, String filehash) {
		JSONObject data = new JSONObject();
		data.put("type", type);
		data.put("sourceip", sourceIP.getHostAddress());
		data.put("destip", destIP.getHostAddress());
		data.put("timestamp", LocalTime.now().toString());
		data.put("datatype", datatype);
		data.put("data", message);
		// fragmentation
		data.put("fragnumber", fragnumber);
		data.put("fragtotal", fragtotal);
		data.put("filehash", filehash);
		return data;
	}
	
	/**
	 * Compose a HELLO packet and return it as a JSON object
	 * @param sourceIP The source InetAddress
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject composeHello(InetAddress sourceIP) {
		JSONObject hello = new JSONObject();
		hello.put("type", NetworkController.TYPE_HELLO);
		hello.put("sourceip", sourceIP.getHostAddress());
		hello.put("timestamp", LocalTime.now().toString());
		return hello;
	}
}
