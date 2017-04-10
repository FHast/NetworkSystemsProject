package connection;

import java.net.InetAddress;
import java.time.LocalTime;

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
		rrep.put("destip", dest.getHostAddress());
		rrep.put("sourceip", source.getHostAddress());
		rrep.put("sourceseq", sourceseq);
		rrep.put("hopcount", hopcount);
		return rrep;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject composeRREQ(InetAddress sourceip, int sourceseq, int broadcastid, InetAddress dest, int destseq, int hopcount) {
		JSONObject rreq = new JSONObject();
		rreq.put("type", RREQservice.RREQ_ID);
		rreq.put("sourceip", sourceip.getHostAddress());
		rreq.put("sourceseq", sourceseq);
		rreq.put("broadcastid", broadcastid);
		rreq.put("destip", dest.getHostAddress());
		rreq.put("destseq", destseq);
		rreq.put("hopcount", hopcount);
		return rreq;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject composeDataText(InetAddress sourceIP, InetAddress destIP, String message) {
		JSONObject data = new JSONObject();
		data.put("type", DATAservice.DATA_ID);
		data.put(sourceIP, sourceIP.getHostAddress());
		data.put("destip", destIP.getHostAddress());
		data.put("timestamp", LocalTime.now().toString());
		data.put("ttl", 64);
		data.put("datatype", DATAservice.DATA_TYPE_TEXT);
		data.put("data", message);
		return data;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject composeRERR(int destCount, InetAddress unreachableDest, int destSeq) {
		JSONObject rerr = new JSONObject();
		rerr.put("type", RERRservice.RERR_ID);
		rerr.put("destCount", destCount);
		rerr.put("unreachableDest", unreachableDest);
		rerr.put("destSeq", unreachableDest); 
		return rerr;
	}
}
