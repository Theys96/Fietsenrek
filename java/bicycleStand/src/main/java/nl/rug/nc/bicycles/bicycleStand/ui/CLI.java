package nl.rug.nc.bicycles.bicycleStand.ui;

import java.io.Console;
import java.util.logging.Logger;

import nl.rug.nc.bicycles.bicycleStand.HeartbeatRunnable;
import nl.rug.nc.bicycles.bicycleStand.model.StandData;
import nl.rug.nc.bicycles.bicycleStand.model.StandData.SlotState;
import nl.rug.nc.bicycles.bicycleStand.network.SocketHandler;

/**
 * This class provides a Command Line Interface for a bicycle stand.
 * It offers ways to edit or display bicycle stand data using Command line commands.
 *
 */
public class CLI extends UI {

	private Console console = System.console();
	
	/**
	 * First checks whether the program is run from a terminal and initializes bicycle stand data.
	 * Then waits for user input. Possible commands are:
	 * <p><ul>
	 * <li> set - Sets a given slot number to a given SlotState.
	 * <li> toggle - Toggles a given slot from EMPTY to FILLED or vice versa.
	 * <li> list - Prints the current data for each slot.
	 * <li> exit - Exits the program.
	 * </ul><p>
	 */
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
					if (!getModel().isLocked(slot) || requestUnlock(slot)) {
						getModel().setSlot(slot, SlotState.valueOf(newState));
					}
				} catch (Exception e) {
					printParseError("set <slot (0-"+getModel().getMaxSlot()+")> <EMPTY | FILLED | RESERVED>");
				}
				break;
			case "toggle":
				try {
					int slot = Integer.valueOf(command[1]);
					if (!getModel().isLocked(slot) || requestUnlock(slot)) {
						getModel().toggleSlot(slot);
					}
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
	
	/**
	 * Request an unlock code from the user for a given slot.
	 * Checks whether it is the correct code.
	 * 
	 * @param slot The slot that needs to be unlocked
	 * @return Boolean True if the code is correct, False otherwise.
	 */
	private boolean requestUnlock(int slot) {
		try {
			int code = Integer.valueOf(prompt("Unlock code: "));
			if (getModel().checkUnlockCode(slot, code)) {
				return true;
			} else {
				System.out.println("Incorrect code!");
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Unlock code needs to be numeric.");
		}
		return false;
	}
	
	private void printParseError(String usage) {
		System.out.println("Error parsing command. Usage: " + usage);
	}
	
	/**
	 * Prompts for bicycle stand data and initializes a bicycle stand with that data.
	 */
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
	
	/**
	 * Reads and returns the password entered by the user.
	 * 
	 * @param prompt The password prompt.
	 * @return The password.
	 */
	private String promptPassword(String prompt) {
		return String.valueOf(console.readPassword(prompt));
	}
	
	/**
	 * Prompts for user input and returns a default String if no input is given.
	 * 
	 * @param prompt The prompt message.
	 * @param defaultString The default input.
	 * @return Returns the user input if it was given, default input otherwise.
	 */
	private String prompt(String prompt, String defaultString) {
		String input = prompt(prompt);
		return input.equals("")? defaultString : input;
	}
	
	/**
	 * Reads and returns the user input to a prompt.
	 * 
	 * @param prompt The prompt message.
	 * @return The user input.
	 */
	private String prompt(String prompt) {
		return console.readLine(prompt);
	}
	
	/* (non-Javadoc)
	 * @see nl.rug.nc.bicycles.bicycleStand.ui.UI#showMessage(nl.rug.nc.bicycles.bicycleStand.ui.UI.MessageType, java.lang.String)
	 */
	@Override
	protected void showMessage(MessageType type, String message) {
		Logger.getLogger("CLI").log(type.getLoggerEquivalent(), message);
	}

}
