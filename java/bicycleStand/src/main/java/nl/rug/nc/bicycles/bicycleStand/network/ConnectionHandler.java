package nl.rug.nc.bicycles.bicycleStand.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import nl.rug.nc.bicycles.bicycleStand.model.StandData;

/**
 * Handles the generation a lock code for a slot number given by a client.
 * It returns the code to the client.
 */
public class ConnectionHandler implements Runnable {
	
	private Socket socket;
	private StandData model;
	
	/**
	 * Initializes the Socket and the StandData.
	 * 
	 * @param socket The socket of the bicycle stand.
	 * @param model The StandData of the bicycle stand.
	 */
	public ConnectionHandler(Socket socket, StandData model) {
		this.socket = socket;
		this.model = model;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			int slot = ois.readInt();
			int code = model.reserveSlot(slot);
			oos.writeInt(code);
			oos.close();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
