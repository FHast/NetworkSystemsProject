package applicationLayer;

import java.net.InetAddress;
import java.time.LocalTime;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import networkLayer.NetworkController;

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
	public static JSONObject composeRREP(InetAddress dest, InetAddress source, long sourceseq, long hopcount) {
		JSONObject rrep = new JSONObject();
		rrep.put("type", NetworkController.RREP_ID);
		rrep.put("destip", dest.getHostAddress());
		rrep.put("sourceip", source.getHostAddress());
		rrep.put("sourceseq", sourceseq);
		rrep.put("hopcount", hopcount);
		return rrep;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject composeRREQ(InetAddress sourceip, long sourceseq, long broadcastid, InetAddress dest,
			long destseq, long hopcount) {
		JSONObject rreq = new JSONObject();
		rreq.put("type", (int) NetworkController.RREQ_ID);
		rreq.put("sourceip", sourceip.getHostAddress());
		rreq.put("sourceseq", sourceseq);
		rreq.put("broadcastid", broadcastid);
		rreq.put("destip", dest.getHostAddress());
		rreq.put("destseq", destseq);
		rreq.put("hopcount", hopcount);
		return rreq;
	}

	public static JSONObject composeDataText(InetAddress sourceIP, InetAddress destIP, String message) {
		return composeData(sourceIP, destIP, message, DataController.DATA_TYPE_TEXT);
	}

	public static JSONObject composeDataAck(InetAddress sourceIP, InetAddress destIP, String message) {
		return composeData(sourceIP, destIP, message, DataController.DATA_TYPE_ACK);
	}

	@SuppressWarnings("unchecked")
	public static JSONObject composeRERR(InetAddress sourceIP, long sourceSeq, InetAddress[] unreachable) {

		JSONArray list = new JSONArray();
		for (InetAddress i : unreachable) {
			list.add(i.getHostAddress());
		}

		JSONObject rerr = new JSONObject();
		rerr.put("type", NetworkController.RERR_ID);
		rerr.put("sourceip", sourceIP.getHostAddress());
		rerr.put("sourceseq", sourceSeq);
		rerr.put("unreachable", list);
		return rerr;
	}

	@SuppressWarnings("unchecked")
	private static JSONObject composeData(InetAddress sourceIP, InetAddress destIP, String message, int type) {
		JSONObject data = new JSONObject();
		data.put("type", DataController.DATA_ID);
		data.put("sourceip", sourceIP.getHostAddress());
		data.put("destip", destIP.getHostAddress());
		data.put("timestamp", LocalTime.now().toString());
		data.put("ttl", 64);
		data.put("datatype", type);
		data.put("data", message);
		return data;
	}
}
