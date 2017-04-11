package networkLayer.tables;

import java.net.InetAddress;
import java.time.LocalTime;

public class FTableEntry {
	private static final int offset = 10;
	
	public InetAddress destinationAddress;
	public InetAddress nextHopAddress;
	public long destinationSequenceNumber;
	public LocalTime lifetime;
	public long hopcount;

	public FTableEntry(InetAddress dest, InetAddress nextHop, long destSeq, long hopCount) {
		destinationAddress = dest;
		nextHopAddress = nextHop;
		destinationSequenceNumber = destSeq;
		lifetime = LocalTime.now().plusSeconds(offset);
		hopcount = hopCount;
	}
}