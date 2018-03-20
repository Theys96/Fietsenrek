package nl.rug.nc.bicycles.bicycleStand;

import java.io.IOException;
import java.util.Arrays;
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
	private String[] connectionInfo;
	private boolean running = true;
	
	public HeartbeatRunnable(GUI gui, String[] connectionInfo) {
		parent = gui;
		this.connectionInfo = connectionInfo;
	}
	
	private void initConnection() {
		ConnectionFactory factory = new ConnectionFactory();
		if (!connectionInfo[0].equals("")) factory.setUsername(connectionInfo[0]);
		if (!connectionInfo[1].equals("")) factory.setPassword(connectionInfo[1]);
		if (!connectionInfo[2].equals("")) {
			factory.setHost(connectionInfo[2]);
		} else {
			factory.setHost("localhost");
		}
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
				} catch (java.net.UnknownHostException uhe) {
					logger.severe("Host not found! Stopping!");
					running = false;
					return;
				} catch (com.rabbitmq.client.AuthenticationFailureException afe) {
					logger.severe("Incorrect username/password for RabbitMQ. Stopping...");
					running = false;
					return;
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
		while (running) {
			long startTime = System.currentTimeMillis();
			try {
				channel.basicPublish("fietsenrek_servers", "", null, ("[\"heartbeat\", \""+parent.getModel().getName()+"\", "+parent.getModel().getSlotDataJson()+"]").getBytes());
			} catch (com.rabbitmq.client.AlreadyClosedException e) {
				// Will retry every second
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000-(System.currentTimeMillis()-startTime));
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		System.exit(1);
	}

}
