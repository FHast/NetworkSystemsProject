package networkLayer.tables;

import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import controller.Controller;

/**
 * This service keeps track of the Reverse Table.
 */
public class ReverseTableService implements Runnable {
	private static ArrayList<RTableEntry> reverseTable = new ArrayList<>();

	/**
	 * Adds an entry to the Reverse Table.
	 * @param source The source InetAddress
	 * @param nextHop The nextHop to that source
	 * @param hopCount hop count
	 */
	public static void addEntry(InetAddress source, InetAddress nextHop, long hopCount) {
		Controller.mainWindow.log("[RTable] Add Entry");
		if (!hasEntry(source)) {
			reverseTable.add(new RTableEntry(source, nextHop, hopCount));
		}
	}

	/**
	 * Checks whether there is already an entry for that destination.
	 * @param dest The given destination InetAddress
	 * @return
	 */
	public static boolean hasEntry(InetAddress dest) {
		for (RTableEntry e : reverseTable) {
			if (e.sourceAddress.equals(dest)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Searches for an entry and returns it.
	 * @param dest The destination InetAddress
	 * @return The entry (if it exists)
	 * @throws NoEntryException No entry has been found
	 */
	public static RTableEntry getEntry(InetAddress dest) throws NoEntryException {
		for (RTableEntry e : reverseTable) {
			if (e.sourceAddress.equals(dest)) {
				return e;
			}
		}
		throw new NoEntryException();
	}

	/**
	 * Removes expired entries.
	 */
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
