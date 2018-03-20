package nl.rug.nc.bicycles.bicycleStand;

import java.io.IOException;
import java.util.logging.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoverableConnection;
import com.rabbitmq.client.RecoveryListener;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import nl.rug.nc.bicycles.bicycleStand.gui.GUI;

public class HeartbeatRunnable implements Runnable {
	
	private GUI parent;
	private Channel channel;
	private RecoverableConnection connection;
	private Logger logger = Logger.getLogger(this.getClass().toString());
	
	public HeartbeatRunnable(GUI gui) {
		parent = gui;
	}
	
	private void initConnection() {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		factory.setAutomaticRecoveryEnabled(true);
		try {
			boolean connected = false;
			while (!connected) {
				try {
					connection = (RecoverableConnection) factory.newConnection();
					connected = true;
				} catch (java.net.ConnectException ce) {
					logger.warning("Could not connect to RabbitMQ server, retrying every 5 seconds...");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}
			}
			logger.info("Connected to RabbitMQ server.");
			connection.addShutdownListener(new ShutdownListener() {

				@Override
				public void shutdownCompleted(ShutdownSignalException arg0) {
					logger.warning("No connection to RabbitMQ server, retrying every 5 seconds...");
				}
				
			});
			connection.addRecoveryListener(new RecoveryListener() {

				@Override
				public void handleRecovery(Recoverable arg0) {
					logger.info("Connection recovered!");
				}

				@Override
				public void handleRecoveryStarted(Recoverable arg0) {
					logger.info("Starting automatic connection recovery...");
				}
				
			});
			channel = connection.createChannel();
			channel.exchangeDeclare("fietsenrek_servers", "fanout", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		if (connection==null) initConnection();
		while (connection.isOpen()) {
			long startTime = System.currentTimeMillis();
			try {
				channel.basicPublish("fietsenrek_servers", "", null, ("[\"heartbeat\", \""+parent.getModel().getName()+"\", "+parent.getModel().getSlotDataJson()+"]").getBytes());
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
