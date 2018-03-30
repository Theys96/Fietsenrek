package nl.rug.nc.bicycles.bicycleStand;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import nl.rug.nc.bicycles.bicycleStand.ui.CLI;
import nl.rug.nc.bicycles.bicycleStand.ui.GUI;

/**
 * Opens a Command-line interface or a Graphical User Interface for the bicycle stand
 * depending on the given command line argument.
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
    	if (args.length != 0 && args[0].equalsIgnoreCase("--cli")) {
    		new CLI();
    	} else if (args.length == 0) {
    		new GUI();
    	} else {
    		System.err.println("Unknown parameters");
    	}
    }
}
