package networkLayer.tables;

import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import controller.Controller;

public class ReverseTableService implements Runnable {
	private static ArrayList<RTableEntry> reverseTable = new ArrayList<>();

	public static void addEntry(InetAddress source, InetAddress nextHop, long hopCount) {
		Controller.mainWindow.log("[RTable] Add Entry");
		if (!hasEntry(source)) {
			reverseTable.add(new RTableEntry(source, nextHop, hopCount));
		}
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

	@Override
	public void run() {
		while (true) {
			// Removing expired entries
			try {
				for (RTableEntry e : reverseTable) {
					if (e.lifetime.isBefore(LocalTime.now())) {
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
