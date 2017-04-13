package networkLayer.tables;

import java.net.InetAddress;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import controller.Controller;

public class ForwardingTableService implements Runnable {
	private static boolean shutdown = false;
	private static ArrayList<FTableEntry> forwardingTable = new ArrayList<>();

	public static void addEntry(InetAddress dest, InetAddress nextHop, long destSeq, long hopCount) {
		Controller.mainWindow.log("[FTable] Add Entry for: " + dest.getHostAddress());
		removeEntry(dest);
		forwardingTable.add(new FTableEntry(dest, nextHop, destSeq, hopCount));
	}

	public static boolean hasEntry(InetAddress dest) {
		for (FTableEntry e : forwardingTable) {
			if (e.destinationAddress.equals(dest)) {
				return true;
			}
		}
		return false;
	}
	
	public static void renewEntry(InetAddress dest) {
		try {
			FTableEntry fe = getEntry(dest);
			fe.lifetime = LocalTime.now().plusSeconds(FTableEntry.OFFSET);
		} catch (NoEntryException e) {
			e.printStackTrace();
		};
	}

	public static void removeEntry(InetAddress dest) {
		try {
			for (FTableEntry e : forwardingTable) {
				if (e.destinationAddress.equals(dest)) {
					forwardingTable.remove(e);
				}
			}
		} catch (ConcurrentModificationException e) {
			// timeout
		}
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
	
	public static ArrayList<FTableEntry> getEntries() {
		return forwardingTable;
	}

	@Override
	public void run() {
		while (!shutdown) {
			// Removing expired entries
			try {
				for (int i = 0; i < forwardingTable.size(); i++) {
					if (forwardingTable.get(i).lifetime.isBefore(LocalTime.now()) || forwardingTable.get(i).hopcount >= 100000) {
						Controller.mainWindow.log("[FTable] Entry removed");
						forwardingTable.remove(forwardingTable.get(i));
					}
				}
			} catch (ConcurrentModificationException | NullPointerException e) {
				// nothing
			}
		}
	}
}
