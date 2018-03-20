package nl.rug.nc.bicycles.bicycleStand.ui;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import nl.rug.nc.bicycles.bicycleStand.model.StandData;

public abstract class UI {
	
	private StandData data = null;
	private int logLevel = 0;
	
	public enum MessageType {
		INFO(0, Level.INFO, JOptionPane.INFORMATION_MESSAGE),
		WARNING(1, Level.WARNING, JOptionPane.WARNING_MESSAGE),
		SEVERE(2, Level.SEVERE, JOptionPane.ERROR_MESSAGE);
		
		private Level levelEquivalent;
		private int optionPaneEquivalent;
		private int logLevel;
		
		private MessageType(int logLevel, Level level, int messageType) {
			levelEquivalent = level;
			optionPaneEquivalent = messageType;
			this.logLevel = logLevel;
		}
		
		public Level getLoggerEquivalent() {
			return levelEquivalent; 
		}
		
		public int getJOptionPaneMessageType() {
			return optionPaneEquivalent;
		}
		
		public int getLogLevel() {
			return logLevel;
		}
	}
	
	public void setLogLevel(MessageType type) {
		setLogLevel(type.getLogLevel());
	}
	
	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}
	
	public int getLogLevel() {
		return logLevel;
	}
	
	public StandData getModel() {
		return data;
	}
	
	public void setModel(StandData model) {
		data = model;
	}
	
	public void log(MessageType type, String message) {
		if (type.getLogLevel() >= this.getLogLevel()) {
			showMessage(type, message);
		} else {
			Logger.getLogger(this.getClass().toString()).log(type.getLoggerEquivalent(), message);
		}
	}
	
	protected abstract void showMessage(MessageType type, String message);

}
