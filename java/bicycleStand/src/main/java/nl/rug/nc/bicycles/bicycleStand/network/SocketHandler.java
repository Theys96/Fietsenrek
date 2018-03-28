package nl.rug.nc.bicycles.bicycleStand.network;

import java.io.IOException;
import java.net.ServerSocket;

import nl.rug.nc.bicycles.bicycleStand.ui.UI;
import nl.rug.nc.bicycles.bicycleStand.ui.UI.MessageType;

public class SocketHandler implements Runnable {
	
	private UI ui;
	
	public SocketHandler(UI ui) {
		this.ui = ui;
	}
	
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
