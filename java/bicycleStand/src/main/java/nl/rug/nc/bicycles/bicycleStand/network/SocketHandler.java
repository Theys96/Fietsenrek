package nl.rug.nc.bicycles.bicycleStand.network;

import java.io.IOException;
import java.net.ServerSocket;

import nl.rug.nc.bicycles.bicycleStand.ui.UI;
import nl.rug.nc.bicycles.bicycleStand.ui.UI.MessageType;

/**
 * Handles the socket creation for the bicycle stands.
 *
 */
public class SocketHandler implements Runnable {
	
	private UI ui;
	
	/**
	 * Initializes the UI.
	 * 
	 * @param ui The UI of a bicycle stand.
	 */
	public SocketHandler(UI ui) {
		this.ui = ui;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try (ServerSocket serverSocket = new ServerSocket(8900)) {
			while (true) {
				new Thread(new ConnectionHandler(serverSocket.accept(), ui.getModel())).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
			ui.log(MessageType.SEVERE, "The socket server has crashed!");
		}
	}

}
