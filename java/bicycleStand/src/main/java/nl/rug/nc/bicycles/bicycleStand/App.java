package nl.rug.nc.bicycles.bicycleStand;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import nl.rug.nc.bicycles.bicycleStand.gui.GUI;

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
        new GUI();
    }
}
