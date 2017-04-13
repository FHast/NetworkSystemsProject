package applicationLayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;

/**
 * The class which sends and receives text over the sockets' in- &
 * outputstreams. Both implemented on clientside and on serverside for each
 * connected client.
 * 
 * @author gereon
 *
 */
public class DATAservice extends Observable implements Runnable {
	// list for packets needing acks. TODO

	// incoming data traffic
	private ServerSocket ssock;

	public static void sendData(InetAddress nexthop, String data) throws IOException {
		// initiate connection to next hop neighbor
		Socket sock = new Socket(nexthop, DataController.DATA_PORT);
		PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
		// write data to output stream
		out.println(data);
		// terminate connection
		out.close();
		sock.close();
	}

	@Override
	public void run() {
		try {
			ssock = new ServerSocket(DataController.DATA_PORT);

			// listen for incoming connections
			while (true) {
				// accept connection
				Socket sock = ssock.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				// read data
				String input = in.readLine();

				setChanged();
				notifyObservers(input);
				clearChanged();
			}
		} catch (BindException e) {
			// nothing
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
