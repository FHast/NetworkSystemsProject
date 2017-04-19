package networkLayer.tables;

import java.net.InetAddress;
import java.security.PublicKey;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import javax.crypto.SecretKey;

import controller.Controller;

public class ForwardingTableService implements Runnable {
	private static ArrayList<FTableEntry> forwardingTable = new ArrayList<>();

	public static void addEntry(InetAddress dest, InetAddress nextHop, long hopCount, PublicKey pkey, SecretKey skey) {

		if (hasEntry(dest)) {
			renewEntry(dest);
		} else {
			removeEntry(dest);
			Controller.mainWindow.log("[FTable] Add Entry for: " + dest.getHostAddress());
			forwardingTable.add(new FTableEntry(dest, nextHop, hopCount, pkey, skey));
		}
	}

	public static boolean hasEntry(InetAddress dest) {
		for (FTableEntry e : forwardingTable) {
			if (e.destinationAddress.equals(dest)) {
				if (e.sessionkey != null) {
					return true; 
				}
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
		}
		;
	}

	public static void removeEntry(InetAddress dest) {
		try {
			FTableEntry fe = null;
			for (FTableEntry current : forwardingTable) {
				if (current.destinationAddress.equals(dest)) {
					fe = current;
				}
			}
			if (fe != null) {
				forwardingTable.remove(fe);
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

	public static ArrayList<FTableEntry> getEntries() {
		return forwardingTable;
	}

	@Override
	public void run() {
		while (true) {
			// Removing expired entries
			try {
				for (int i = 0; i < forwardingTable.size(); i++) {
					if (forwardingTable.get(i).lifetime.isBefore(LocalTime.now())) {

						removeEntry(forwardingTable.get(i).destinationAddress);
					}
				}
			} catch (ConcurrentModificationException | NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
}
