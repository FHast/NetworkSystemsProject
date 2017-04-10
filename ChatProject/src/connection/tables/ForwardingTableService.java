package connection.tables;

import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class ForwardingTableService implements Runnable {
	private static boolean shutdown = false;
	private static ArrayList<FTableEntry> forwardingTable = new ArrayList<>();

	public static void addEntry(InetAddress dest, InetAddress nextHop, long destSeq, long hopCount) {
		System.out.println("[FTable] Add Entry");
		forwardingTable.add(new FTableEntry(dest, nextHop, destSeq, hopCount));
	}

	public static boolean hasEntry(InetAddress dest) {
		for (FTableEntry e : forwardingTable) {
			//System.out.println(e.destinationAddress);
			//System.out.println(dest);
			if (e.destinationAddress.equals(dest)) {
				return true;
			}
		}
		return false;
	}

	public static FTableEntry getEntry(InetAddress dest) throws NoEntryException {
		for (FTableEntry e : forwardingTable) {
			if (e.destinationAddress.equals(dest)) {
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
					if (e.lifetime.isBefore(LocalTime.now())) {
						System.out.println("[FTable] Entry removed");
						forwardingTable.remove(e);
					}
				}
			} catch (ConcurrentModificationException e) {
				// nothing
			}
		}
	}
}
