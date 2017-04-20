package networkLayer.tables;

import java.net.InetAddress;
import java.time.LocalTime;

/**
 * This class models an entry in the Reverse Table.
 */
public class RTableEntry {
	private static final int offset = 10;

	public InetAddress sourceAddress;
	public InetAddress nextHopAddress;
	public long hopsToSource;
	public LocalTime lifetime;

	public RTableEntry(InetAddress source, InetAddress nextHop, long hopCount) {
		sourceAddress = source;
		nextHopAddress = nextHop;
		hopsToSource = hopCount;
		lifetime = LocalTime.now().plusSeconds(offset);
	}
}