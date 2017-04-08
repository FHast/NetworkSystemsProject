package connection;

import java.net.InetAddress;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONservice {
	public static JSONObject getJson(String s) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONArray array = (JSONArray) parser.parse(s);
		JSONObject json = (JSONObject) array.get(1);
		return json;
	}

	@SuppressWarnings("unchecked")
	public static JSONObject composeRREP(InetAddress dest, InetAddress source, int sourceseq, int hopcount) {
		JSONObject rrep = new JSONObject();
		rrep.put("type", RREPservice.RREP_ID);
		rrep.put("destip", dest);
		rrep.put("sourceip", source);
		rrep.put("sourceseq", sourceseq);
		rrep.put("hopcount", hopcount);
		return rrep;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject composeRREQ(InetAddress sourceip, int sourceseq, int broadcastid, InetAddress dest, int destseq, int hopcount) {
		JSONObject rreq = new JSONObject();
		rreq.put("type", RREQservice.RREQ_ID);
		rreq.put("sourceip", sourceip);
		rreq.put("sourceseq", sourceseq);
		rreq.put("broadcastid", broadcastid);
		rreq.put("dest", dest);
		rreq.put("destseq", destseq);
		rreq.put("hopcount", hopcount);
		return rreq;
	}
}
