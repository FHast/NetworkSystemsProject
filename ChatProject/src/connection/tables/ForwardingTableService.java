package connection.tables;

import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class ForwardingTableService implements Runnable {
	private static boolean shutdown = false;
	private static ArrayList<FTableEntry> forwardingTable = new ArrayList<>();

	public static void addEntry(InetAddress dest, InetAddress nextHop, int destSeq, int hopCount) {
		forwardingTable.add(new FTableEntry(dest, nextHop, destSeq, hopCount));
	}

	public static boolean hasEntry(InetAddress dest) {
		for (FTableEntry e : forwardingTable) {
			if (e.destinationAddress == dest) {
				return true;
			}
		}
		return false;
	}

	public static FTableEntry getEntry(InetAddress dest) throws NoEntryException {
		for (FTableEntry e : forwardingTable) {
			if (e.destinationAddress == dest) {
				return e;
			}
		}
		throw new NoEntryException();
	}

	public void shutdown() {
		shutdown = true;
	}

	@Override
	public void run() {
		while (!shutdown) {
			// Removing expired entries
			try {
				for (FTableEntry e : forwardingTable) {
					if (e.lifetime.isAfter(LocalTime.now())) {
						forwardingTable.remove(e);
					}
				}
			} catch (ConcurrentModificationException e) {
				// nothing
			}
		}
	}
}
