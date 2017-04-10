package connection.tables;

import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import connection.Controller;

public class ForwardingTableService implements Runnable {
	private static boolean shutdown = false;
	private static ArrayList<FTableEntry> forwardingTable = new ArrayList<>();

	public static void addEntry(InetAddress dest, InetAddress nextHop, long destSeq, long hopCount) {
		Controller.mainWindow.log("[FTable] Add Entry");
		forwardingTable.add(new FTableEntry(dest, nextHop, destSeq, hopCount));
	}

	public static boolean hasEntry(InetAddress dest) {
		for (FTableEntry e : forwardingTable) {
			// Controller.mainWindow.log(e.destinationAddress);
			// Controller.mainWindow.log(dest);
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

	public static InetAddress[] getNodes(InetAddress nextHop) {
		ArrayList<InetAddress> unreachable = new ArrayList<>();
		for (FTableEntry e : forwardingTable) {
			if (e.nextHopAddress.equals(nextHop)) {
				unreachable.add(e.destinationAddress);
			}
		}
		InetAddress[] result = new InetAddress[unreachable.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = unreachable.get(i);
		}
		return result;
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
					if (e.lifetime.isBefore(LocalTime.now()) || e.hopcount >= 100000) {
						Controller.mainWindow.log("[FTable] Entry removed");
						forwardingTable.remove(e);
					}
				}
			} catch (ConcurrentModificationException e) {
				// nothing
			}
		}
	}
}
