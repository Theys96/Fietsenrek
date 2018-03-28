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
			DataRequest request = (DataRequest) ois.readObject();
			Object returnValue;
			switch (request.getRequestType()) {
				case SLOT_TIME:
					oos.writeLong(model.getSlotTime(request.getSlot()));
					break;
				case SLOT_RESERVE:
					if (model.getSlot(request.getSlot()) == SlotState.EMPTY) {
						model.setSlot(request.getSlot(), SlotState.RESERVED);
						returnValue = true;
					} else {
						returnValue = false;
					}
					break;
			}
			oos.close();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
