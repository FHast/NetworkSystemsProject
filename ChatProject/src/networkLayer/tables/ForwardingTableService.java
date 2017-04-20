package networkLayer.tables;

import java.net.InetAddress;
import java.security.PublicKey;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import javax.crypto.SecretKey;

import controller.Controller;

/**
 * This service keeps track of the Forwarding Table.
 */
public class ForwardingTableService implements Runnable {
	private static ArrayList<FTableEntry> forwardingTable = new ArrayList<>();

	/**
	 * Adds an entry to the FT.
	 * @param dest The destination
	 * @param nextHop The next hop when trying to reach the destination
	 * @param hopCount hop count
	 * @param pkey Public Key of the destination
	 * @param skey Secret key used to communicate with that destination
	 */
	public static void addEntry(InetAddress dest, InetAddress nextHop, long hopCount, PublicKey pkey, SecretKey skey) {

		if (hasEntry(dest)) {
			renewEntry(dest);
		} else {
			removeEntry(dest);
			Controller.mainWindow.log("[FTable] Add Entry for: " + dest.getHostAddress());
			forwardingTable.add(new FTableEntry(dest, nextHop, hopCount, pkey, skey));
		}
	}

	/**
	 * Checks whether an entry for that address exists.
	 * @param dest The entry that is looked up
	 * @return
	 */
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

	/**
	 * Extends the TTL of the entry.
	 * @param dest The destination address of that entry
	 */
	public static void renewEntry(InetAddress dest) {
		try {
			FTableEntry fe = getEntry(dest);
			fe.lifetime = LocalTime.now().plusSeconds(FTableEntry.OFFSET);
		} catch (NoEntryException e) {
			e.printStackTrace();
		}
		;
	}

	/**
	 * Removes that entry from the FT.
	 * @param dest The entry to be removed
	 */
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

	/**
	 * Searches for entry and returns it.
	 * @param dest The destination to look for
	 * @return The entry
	 * @throws NoEntryException No entry has been found
	 */
	public static FTableEntry getEntry(InetAddress dest) throws NoEntryException {
		for (FTableEntry e : forwardingTable) {
			if (e.destinationAddress.equals(dest)) {
				return e;
			}
		}
		throw new NoEntryException();
	}

	/**
	 * Search for destinations that have the same nextHop in the FT.
	 * @param nextHop The nextHop that they share in the FT.
	 * @return 
	 */
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

	/**
	 * Returns the whole FT.
	 * @return
	 */
	public static ArrayList<FTableEntry> getEntries() {
		return forwardingTable;
	}

	/**
	 * Removes expired entries.
	 */
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
