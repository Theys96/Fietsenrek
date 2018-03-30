package nl.rug.nc.bicycles.bicycleStand.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * This class handles the storage, creation and formating of all the data of a bicycle stand.
 *
 */
public class StandData extends Observable {
	
	private long[] data;
	private String name;
	private Map<Integer, Integer> lockCodes = new HashMap<>();
	private Map<Integer, Long> reservedTimes = new HashMap<>();
	private Random random = new Random();
	private ScheduledExecutorService reservedRemoverService = Executors.newSingleThreadScheduledExecutor();
	
	/**
	 * Specifies an empty, reserved and filled slot state 
	 * with their corresponding Long values.
	 *
	 */
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
	
	/**
	 * Creates a Long array where the size is the number of slots. 
	 * The bicycle name is also assigned and a ScheduledExecutorService is 
	 * created that auto-removes reservations after 90 minutes.
	 * 
	 * @param name The name of the bicycle stand.
	 * @param slots The number of slots of the bicycle stand.
	 */
	public StandData(String name, int slots) {
		data = new long[slots];
		this.name = name;
		reservedRemoverService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Map<Integer, Long> times = StandData.this.getReservedTimes();
				Set<Integer> toRemove = new HashSet<>();
				synchronized (times) {
					for (Integer key : times.keySet()) {
						if (times.get(key)+5400000 < System.currentTimeMillis()) toRemove.add(key);
					}
					for (Integer key: toRemove) {
						times.remove(key);
						setSlot(key, SlotState.EMPTY);
					}
				}
			}
		}, 0, 60, TimeUnit.SECONDS);
	}
	
	/**
	 * Creates an unlock code for a slot and puts it in a Map.
	 * 
	 * @param slot The slot number of the bicycle stand.
	 * 
	 * @return code The four digit code for a given slot.
	 */
	public int lockSlot(int slot) {
		int code = random.nextInt(8999)+1001;
		lockCodes.put(slot, code);
		return code;
	}
	
	private Map<Integer, Long> getReservedTimes() {
		return reservedTimes;
	}
	
	/**
	 * Converts the slot data of the bicycle stand to a JSON format and returns it.
	 * 
	 * @return A JSON format String that contains the slot data
	 */
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
	
	/**
	 * Checks whether a given code is the correct code for a given slot number
	 * 
	 * @param slot A slot number.
	 * @param code A given code number.
	 * 
	 * @return Boolean True if the code is correct, False otherwise.
	 */
	public boolean checkUnlockCode(int slot, int code) {
		return lockCodes.containsKey(slot) && lockCodes.get(slot) == code;
	}

	/**
	 * Sets a given slot to a given slot state.
	 * 
	 * @param slot The slot number.
	 * @param state The SlotState the slot should be in.
	 */
	public void setSlot(int slot, SlotState state) {
		lockCodes.remove(slot);
		synchronized (reservedTimes) {
			reservedTimes.remove(slot);
			if (state == SlotState.RESERVED) reservedTimes.put(slot, System.currentTimeMillis());
		}
		synchronized (data) {
			data[slot] = state.getValue();
		}
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Returns the SlotState of a given slot
	 * 
	 * @param slot the slot number
	 * 
	 * @return the SlotState that corresponds with the value in data for the given slot.
	 */
	public SlotState getSlot(int slot) {
		synchronized (data) {
			if (data[slot] == 0) return SlotState.EMPTY;
			if (data[slot] == -1) return SlotState.RESERVED;
			return SlotState.FILLED;
		}
	}
	
	/**
	 * Returns the time in milliseconds a given slot has been occupied.
	 * 
	 * @param slot The slot number.
	 * 
	 * @return Occupation time in milliseconds
	 */
	public long getSlotTime(int slot) {
		if (getSlot(slot) != SlotState.FILLED) return -1;
		synchronized (data) {
			return System.currentTimeMillis()-data[slot];
		}
	}
	
	/**
	 * Changes the SlotState of a given slot number from FILLED to EMPTY or vice versa.
	 * 
	 * @param slot The slot number.
	 */
	public void toggleSlot(int slot) {
		setSlot(slot, getSlot(slot) == SlotState.FILLED? SlotState.EMPTY : SlotState.FILLED);
	}
	
	/**
	 * Returns the number of filled slots.
	 * 
	 * @return total The number of filled slots.
	 */
	public int getFilledSlotCount() {
		synchronized (data) {
			int total = 0;
			for (int i=0; i<data.length; i++) {
				if (data[i]!=0) total++;
			}
			return total;
		}
	}
	
	/**
	 * Returns the number of unoccupied slots.
	 * 
	 * @return Number of free slots.
	 */
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
