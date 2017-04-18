package networkLayer;

import java.net.InetAddress;
import java.security.PublicKey;
import java.time.LocalTime;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import applicationLayer.DataController;

public class JSONservice {

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

	@SuppressWarnings("unchecked")
	public static JSONObject composeRREP(InetAddress dest, InetAddress source, long hopcount, PublicKey publicKey, String sessionKey) {
		JSONObject rrep = new JSONObject();
		rrep.put("type", NetworkController.TYPE_RREP);
		rrep.put("destip", dest.getHostAddress());
		rrep.put("sourceip", source.getHostAddress());
		rrep.put("hopcount", hopcount);
		rrep.put("publickey", publicKey);
		rrep.put("sessionkey", sessionKey);
		return rrep;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject composeRREQ(InetAddress sourceip, long broadcastid, InetAddress dest, long hopcount, PublicKey publicKey) {
		JSONObject rreq = new JSONObject();
		rreq.put("type", (int) NetworkController.TYPE_RREQ);
		rreq.put("sourceip", sourceip.getHostAddress());
		rreq.put("broadcastid", broadcastid);
		rreq.put("destip", dest.getHostAddress());
		rreq.put("hopcount", hopcount);
		rreq.put("publickey", publicKey);
		return rreq;
	}

	public static JSONObject composeDataText(InetAddress sourceIP, InetAddress destIP, String message) {
		return composeData(sourceIP, destIP, message, "" + DataController.DATA_TYPE_TEXT, NetworkController.TYPE_DATA);
	}

	public static JSONObject composeAck(InetAddress sourceIP, InetAddress destIP, String message) {
		return composeData(sourceIP, destIP, message, "", NetworkController.TYPE_ACK);
	}

	public static JSONObject composeDataFile(InetAddress sourceIP, InetAddress destIP, String data, String appendix) {
		return composeData(sourceIP, destIP, data, appendix, NetworkController.TYPE_DATA);
	}

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

	@SuppressWarnings("unchecked")
	private static JSONObject composeData(InetAddress sourceIP, InetAddress destIP, String message, String datatype, int type) {
		JSONObject data = new JSONObject();
		data.put("type", type);
		data.put("sourceip", sourceIP.getHostAddress());
		data.put("destip", destIP.getHostAddress());
		data.put("timestamp", LocalTime.now().toString());
		data.put("datatype", datatype);
		data.put("data", message);
		return data;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject composeHello(InetAddress sourceIP) {
		JSONObject hello = new JSONObject();
		hello.put("type", NetworkController.TYPE_HELLO);
		hello.put("sourceip", sourceIP.getHostAddress());
		hello.put("timestamp", LocalTime.now().toString());
		return hello;
	}
}
