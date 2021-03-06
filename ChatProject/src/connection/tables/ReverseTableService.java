package connection.tables;

import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import connection.Controller;

public class ReverseTableService implements Runnable {
	private static boolean shutdown = false;
	private static ArrayList<RTableEntry> reverseTable = new ArrayList<>();

	public static void addEntry(InetAddress source, InetAddress nextHop, long sourceSeq, long hopCount) {
		Controller.mainWindow.log("[RTable] Add Entry");
		reverseTable.add(new RTableEntry(source, nextHop, sourceSeq, hopCount));
	}

	public static boolean hasEntry(InetAddress dest) {
		for (RTableEntry e : reverseTable) {
			if (e.sourceAddress.equals(dest)) {
				return true;
			}
		}
		return false;
	}

	public static RTableEntry getEntry(InetAddress dest) throws NoEntryException {
		for (RTableEntry e : reverseTable) {
			if (e.sourceAddress.equals(dest)) {
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
				for (RTableEntry e : reverseTable) {
					if (e.lifetime.isAfter(LocalTime.now())) {
						reverseTable.remove(e);
						break;
					}
				}
			} catch (ConcurrentModificationException e) {
				// nothing
			}
		}
	}
}
