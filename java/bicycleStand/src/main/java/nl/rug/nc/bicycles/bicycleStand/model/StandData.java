package nl.rug.nc.bicycles.bicycleStand.model;

import java.util.Arrays;
import java.util.Observable;
import java.util.stream.IntStream;

public class StandData extends Observable {
	
	private int[] data;
	private String name;
	
	public StandData(String name, int slots) {
		data = new int[slots];
		this.name = name;
	}
	
	public String getSlotDataJson() {
		return Arrays.toString(data);
	}

	public void setSlot(int slot, int state) {
		data[slot] = state;
		setChanged();
		notifyObservers();
	}
	
	public int getSlot(int slot) {
		return data[slot];
	}
	
	public void toggleSlot(int slot) {
		setSlot(slot, getSlot(slot) == 1? 0 : 1);
	}
	
	public int getFilledSlotCount() {
		return IntStream.of(data).sum();
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
