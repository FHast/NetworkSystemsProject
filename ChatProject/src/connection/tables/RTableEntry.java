package connection.tables;

import java.net.InetAddress;
import java.time.LocalTime;

public class RTableEntry {
	private static final int offset = 5;

	public InetAddress sourceAddress;
	public int sourceSequenceNumber;
	public InetAddress nextHopAddress;
	public int hopsToSource;
	public LocalTime lifetime;

	public RTableEntry(InetAddress source, InetAddress nextHop, int sourceSeq, int hopCount) {
		sourceAddress = source;
		sourceSequenceNumber = sourceSeq;
		nextHopAddress = nextHop;
		hopsToSource = hopCount;
		lifetime = LocalTime.now().plusSeconds(offset);
	}
}