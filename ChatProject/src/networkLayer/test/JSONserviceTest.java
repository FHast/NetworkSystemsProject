package networkLayer.test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import networkLayer.JSONservice;

public class JSONserviceTest {

	private static String s;
	private static InetAddress dest;
	private static InetAddress source;
	private static long hopcount;
	private static String sessionKey;
	private static InetAddress sourceip;
	private static long broadcastid;
	private static String message;
	private static String data;
	private static String appendix;
	private static int fragnumber;
	private static int fragtotal;
	private static String filehash;
	private static InetAddress[] unreachable;

	@BeforeClass
	public static void create() {
		// Initialize variables.
		try {
			s = "{\"destip\":\"192.168.5.3\",\"fragnumber\":0,\"fragtotal\":0,\"sourceip\":\"192.168.5.1\",\"data\":\"626c5d1b6fb94efdcdd4594bf13ead797d30a9b3e172dd7d4142b6edf88c1d60\",\"datatype\":\"\",\"filehash\":\"\",\"type\":5,\"timestamp\":\"12:00:44.348\"}";
			dest = InetAddress.getByName("192.168.5.1");
			source = InetAddress.getByName("192.168.5.3");
			hopcount = 2;
			sessionKey = "key";
			sourceip = InetAddress.getByName("192.168.5.3");
			broadcastid = 4;
			message = "hello";
			data = "data";
			appendix = "png";
			fragnumber = 2;
			fragtotal = 3;
			filehash = "hash";
			unreachable = new InetAddress[] {InetAddress.getByName("192.168.5.3"), InetAddress.getByName("192.168.5.1")};
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void getJsonTest() throws ParseException {
		// Test if getJson returns something.
		Assert.assertNotNull(JSONservice.getJson(s));
	}

	@Test
	public void composeRREPTest() {
		// Test if composeRREP returns something.
		Assert.assertNotNull(JSONservice.composeRREP(dest, source, hopcount, sessionKey));
	}
	
	@Test
	public void composeRREQTest() {
		// Test if composeRREQ returns something.
		Assert.assertNotNull(JSONservice.composeRREQ(sourceip, broadcastid, dest, hopcount));
	}
	
	@Test
	public void composeDataTextTest() {
		// Test if composeDataText returns something.
		Assert.assertNotNull(JSONservice.composeDataText(source, dest, message));
	}
	
	@Test
	public void composeAckTest() {
		// Test if composeAck returns something.
		Assert.assertNotNull(JSONservice.composeAck(source, dest, message));
	}
	
	@Test
	public void composeDataFileTest() {
		// Test if composeDataFile returns something.
		Assert.assertNotNull(JSONservice.composeDataFile(source, dest, data, appendix, fragnumber, fragtotal, filehash));
	}
	
	@Test
	public void composeRERRTest() {
		// Test if composeRERR returns something.
		Assert.assertNotNull(JSONservice.composeRERR(source, unreachable));
	}
	
	@Test
	public void composeHelloTest() {
		// Test if composeHello returns something.
		Assert.assertNotNull(JSONservice.composeHello(source));
	}
}