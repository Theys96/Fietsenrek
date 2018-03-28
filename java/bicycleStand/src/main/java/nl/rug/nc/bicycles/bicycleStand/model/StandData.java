package nl.rug.nc.bicycles.bicycleStand.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.function.LongSupplier;

public class StandData extends Observable {
	
	private long[] data;
	private String name;
	private Map<Integer, Integer> lockCodes = new HashMap<>();
	private Random random = new Random();
	
	public enum SlotState {
		EMPTY(() -> 0),
		RESERVED(() -> -1),
		FILLED(System::currentTimeMillis);
		
		private LongSupplier call;
		
		private SlotState(LongSupplier call) {
			this.call = call;
		}
		
		public long getValue() {
			return call.getAsLong();
		}
	}
	
	public StandData(String name, int slots) {
		data = new long[slots];
		this.name = name;
	}
	
	public int lockSlot(int slot) {
		int code = random.nextInt(8999)+1001;
		lockCodes.put(slot, code);
		return code;
	}
	
	public String getSlotDataJson() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (int i=0; i < data.length; i++) {
			switch (getSlot(i)) {
				case EMPTY:
					builder.append(0);
					break;
				case RESERVED:
					builder.append(-1);
					break;
				case FILLED:
					builder.append(getSlotTime(i));
					break;
			}
			if (i < data.length - 1) {
				builder.append(", ");
			}
		}
		builder.append("]");
		return builder.toString();
	}
	
	public boolean isLocked(int slot) {
		return lockCodes.containsKey(slot);
	}
	
	public boolean checkUnlockCode(int slot, int code) {
		return lockCodes.containsKey(slot) && lockCodes.get(slot) == code;
	}

	public void setSlot(int slot, SlotState state) {
		lockCodes.remove(slot);
		synchronized (data) {
			data[slot] = state.getValue();
		}
		setChanged();
		notifyObservers();
	}
	
	public SlotState getSlot(int slot) {
		synchronized (data) {
			if (data[slot] == 0) return SlotState.EMPTY;
			if (data[slot] == -1) return SlotState.RESERVED;
			return SlotState.FILLED;
		}
	}
	
	public long getSlotTime(int slot) {
		if (getSlot(slot) != SlotState.FILLED) return -1;
		synchronized (data) {
			return System.currentTimeMillis()-data[slot];
		}
	}
	
	public void toggleSlot(int slot) {
		setSlot(slot, getSlot(slot) == SlotState.FILLED? SlotState.EMPTY : SlotState.FILLED);
	}
	
	public int getFilledSlotCount() {
		synchronized (data) {
			int total = 0;
			for (int i=0; i<data.length; i++) {
				if (data[i]!=0) total++;
			}
			return total;
		}
	}
	
	public int getFreeSlotCount() {
		return data.length-getFilledSlotCount();
	}
	
	public String getName() {
		return name;
	}
	
	public int getMaxSlot() {
		return data.length-1;
	}
	
}
