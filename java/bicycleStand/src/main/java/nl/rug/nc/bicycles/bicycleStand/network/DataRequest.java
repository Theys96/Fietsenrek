package nl.rug.nc.bicycles.bicycleStand.network;

import java.io.Serializable;

public class DataRequest implements Serializable {
	
	static final long serialVersionUID = 6600231573865797022L;
	private RequestType type;
	private int slot;

	public enum RequestType {
		SLOT_TIME,
		SLOT_RESERVE
	}
	
	public DataRequest(RequestType type, int slot) {
		this.type = type;
		this.slot = slot;
	}
	
	public RequestType getRequestType() {
		return type;
	}
	
	public int getSlot() {
		return slot;
	}

}