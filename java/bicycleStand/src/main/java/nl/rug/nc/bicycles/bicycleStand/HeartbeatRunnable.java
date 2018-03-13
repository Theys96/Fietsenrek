package nl.rug.nc.bicycles.bicycleStand;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import nl.rug.nc.bicycles.bicycleStand.gui.GUI;

public class HeartbeatRunnable implements Runnable {
	
	private GUI parent;
	private Channel channel;
	
	public HeartbeatRunnable(GUI gui) {
		parent = gui;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		try {
			Connection connection = factory.newConnection();
			channel = connection.createChannel();
			channel.exchangeDeclare("fietsenrek_servers", "fanout", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (true) {
			long startTime = System.currentTimeMillis();
			try {
				channel.basicPublish("fietsenrek_servers", "", null, ("[\"heartbeat\", \""+parent.getRackName()+"\", "+parent.getSlotDataJson()+"]").getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000-(System.currentTimeMillis()-startTime));
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

}
