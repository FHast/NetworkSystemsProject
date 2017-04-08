package connection.tables;

import java.net.InetAddress;
import java.time.LocalTime;

public class FTableEntry {
	private static final int offset = 10;
	
	public InetAddress destinationAddress;
	public InetAddress nextHopAddress;
	public int destinationSequenceNumber;
	public LocalTime lifetime;
	public int hopcount;

	public FTableEntry(InetAddress dest, InetAddress nextHop, int destSeq, int hopCount) {
		destinationAddress = dest;
		nextHopAddress = nextHop;
		destinationSequenceNumber = destSeq;
		lifetime = LocalTime.now().plusSeconds(offset);
		hopcount = hopCount;
	}
}