package nl.rug.nc.bicycles.bicycleStand.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import nl.rug.nc.bicycles.bicycleStand.model.StandData;
import nl.rug.nc.bicycles.bicycleStand.model.StandData.SlotState;

public class ConnectionHandler implements Runnable {
	
	private Socket socket;
	private StandData model;
	
	public ConnectionHandler(Socket socket, StandData model) {
		this.socket=socket;
		this.model = model;
	}
	
	@Override
	public void run() {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			int slot = ois.readInt();
			int code = -1;
			if (model.getSlot(slot) == SlotState.EMPTY) {
				model.setSlot(slot, SlotState.RESERVED);
				code = model.lockSlot(slot);
			}
			oos.writeInt(code);
			oos.close();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
