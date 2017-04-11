package networkLayer.tables;

import java.net.InetAddress;
import java.time.LocalTime;

public class RTableEntry {
	private static final int offset = 5;

	public InetAddress sourceAddress;
	public long sourceSequenceNumber;
	public InetAddress nextHopAddress;
	public long hopsToSource;
	public LocalTime lifetime;

	public RTableEntry(InetAddress source, InetAddress nextHop, long sourceSeq, long hopCount) {
		sourceAddress = source;
		sourceSequenceNumber = sourceSeq;
		nextHopAddress = nextHop;
		hopsToSource = hopCount;
		lifetime = LocalTime.now().plusSeconds(offset);
	}
}