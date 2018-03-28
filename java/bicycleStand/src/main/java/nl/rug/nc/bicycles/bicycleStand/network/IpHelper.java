package nl.rug.nc.bicycles.bicycleStand.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class IpHelper {
	
	public static String getExternalIP() {
		String ip = null;
	    
	    try {
	    	URL url = new URL("http://checkip.amazonaws.com");
		    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	    	ip = in.readLine();
		    in.close();
	    } catch (IOException ioe) {
	    	ioe.printStackTrace();
	    }
	    
	    return ip;
	}

}
