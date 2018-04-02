package nl.rug.nc.bicycles.bicycleStand.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Checks and returns the external IP Address of the network that the bicycle stand is running on.
 *
 */
public class IpHelper {
	
	/**
	 * Reads and returns the external IP address of the network that the bicycle stand is running on.
	 * 
	 * @return ip The external IP address.
	 */
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
	
	/**
	 * Tries to get the current local ip adress at which this client can be reached.
	 * 
	 * @return "localhost" if no ip could be found, an ip adress as string otherwise
	 */
	public static String getLikelyLocalIp() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();
				if (!networkInterface.isLoopback() && networkInterface.isUp()) {
					for(InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
						if (address instanceof Inet4Address) return address.getHostAddress();
					}
				}
			}
		} catch (SocketException se) {
			se.printStackTrace();
		}
		return "localhost";
	}

}