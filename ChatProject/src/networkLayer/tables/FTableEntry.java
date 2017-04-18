package networkLayer.tables;

import java.net.InetAddress;
import java.time.LocalTime;

public class FTableEntry {
	public static final int OFFSET = 10;
	
	public InetAddress destinationAddress;
	public InetAddress nextHopAddress;
	public LocalTime lifetime;
	public long hopcount;

	public FTableEntry(InetAddress dest, InetAddress nextHop, long hopCount) {
		destinationAddress = dest;
		nextHopAddress = nextHop;
		lifetime = LocalTime.now().plusSeconds(OFFSET);
		hopcount = hopCount;
	}
}