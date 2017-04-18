package networkLayer.tables;

import java.net.InetAddress;
import java.security.PublicKey;
import java.time.LocalTime;

import javax.crypto.SecretKey;

public class FTableEntry {
	public static final int OFFSET = 10;
	
	public InetAddress destinationAddress;
	public InetAddress nextHopAddress;
	public LocalTime lifetime;
	public long hopcount;
	public PublicKey publickey;
	public SecretKey sessionkey;

	public FTableEntry(InetAddress dest, InetAddress nextHop, long hopCount, PublicKey pkey, SecretKey skey) {
		destinationAddress = dest;
		nextHopAddress = nextHop;
		lifetime = LocalTime.now().plusSeconds(OFFSET);
		hopcount = hopCount;
		publickey = pkey;
		sessionkey = skey;
	}
}