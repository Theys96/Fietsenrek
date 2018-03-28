package nl.rug.nc.bicycles.bicycleStand.ui;

import java.io.Console;
import java.util.logging.Logger;

import nl.rug.nc.bicycles.bicycleStand.HeartbeatRunnable;
import nl.rug.nc.bicycles.bicycleStand.model.StandData;
import nl.rug.nc.bicycles.bicycleStand.model.StandData.SlotState;
import nl.rug.nc.bicycles.bicycleStand.network.SocketHandler;

public class CLI extends UI {

	private Console console = System.console();
	
	public CLI() {
		if (console==null) {
			System.err.println("Please run this program from a terminal.");
			System.exit(1);
		}
		init();
		String input = "";
		while (true) {
			input = prompt("");
			String[] command = input.split(" ");
			if (command[0].equalsIgnoreCase("exit")) break;
			switch (command[0].toLowerCase()) {
			case "set":
				try {
					int slot = Integer.valueOf(command[1]);
					String newState = command[2];
					if (getModel().isLocked(slot)) {
						try {
							int code = Integer.valueOf(prompt("Unlock code: "));
							if (getModel().checkUnlockCode(slot, code)) {
								getModel().setSlot(slot, SlotState.valueOf(newState));
							} else {
								System.out.println("Incorrect code!");
							}
						} catch (NumberFormatException nfe) {
							System.out.println("Unlock code needs to be numeric.");
						}
					} else {
						getModel().setSlot(slot, SlotState.valueOf(newState));
					}
				} catch (Exception e) {
					printParseError("set <slot (0-"+getModel().getMaxSlot()+")> <EMPTY | FILLED | RESERVED>");
				}
				break;
			case "toggle":
				try {
					int slot = Integer.valueOf(command[1]);
					getModel().toggleSlot(slot);
				} catch (Exception e) {
					printParseError("toggle <slot (0-"+getModel().getMaxSlot()+")>");
				}
				break;
			case "list":
				System.out.println(getModel().getSlotDataJson());
				break;
			default:
				System.out.println("Unknown command, try \"set\", \"toggle\" or \"list\". Use \"exit\" to exit.");
				break;
			}
		}
		System.out.println("Goodbye!");
		System.exit(0);
	}
	
	private void printParseError(String usage) {
		System.out.println("Error parsing command. Usage: " + usage);
	}
	
	public void init() {
		String name = prompt("Stand name: ");
		int slots = 0;
		while (slots == 0) {
			try {
				slots = Integer.valueOf(prompt("Number of slots: "));
			} catch (NumberFormatException nfe) {
				// Expected
				System.out.println("This number could not be parsed, please make sure you enter a valid positive integer higher than zero.");
			}
		}
		this.setModel(new StandData(name, slots));
		String host = prompt("IP: ", "localhost");
		String user = prompt("Username: ");
		String pass = promptPassword("Password: ");
		new Thread(new HeartbeatRunnable(this, new String[] {user, pass, host})).start();
		new Thread(new SocketHandler(this)).start();
	}
	
	private String promptPassword(String prompt) {
		return String.valueOf(console.readPassword(prompt));
	}
	
	private String prompt(String prompt, String defaultString) {
		String input = prompt(prompt);
		return input.equals("")? defaultString : input;
	}
	
	private String prompt(String prompt) {
		return console.readLine(prompt);
	}
	
	@Override
	protected void showMessage(MessageType type, String message) {
		Logger.getLogger("CLI").log(type.getLoggerEquivalent(), message);
	}

}
