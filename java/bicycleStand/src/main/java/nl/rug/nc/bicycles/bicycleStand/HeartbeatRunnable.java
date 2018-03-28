package nl.rug.nc.bicycles.bicycleStand;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoverableConnection;
import com.rabbitmq.client.RecoveryListener;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import nl.rug.nc.bicycles.bicycleStand.network.IpHelper;
import nl.rug.nc.bicycles.bicycleStand.ui.UI;
import nl.rug.nc.bicycles.bicycleStand.ui.UI.MessageType;

public class HeartbeatRunnable implements Runnable {
	
	private UI parent;
	private Channel channel;
	private RecoverableConnection connection;
	private String[] connectionInfo;
	private boolean running = true;
	private String ip = IpHelper.getExternalIP();
	
	public HeartbeatRunnable(UI ui, String[] connectionInfo) {
		parent = ui;
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
					parent.log(MessageType.WARNING, "Could not connect to RabbitMQ server, retrying every 5 seconds...");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				} catch (java.net.UnknownHostException uhe) {
					parent.log(MessageType.SEVERE, "Host not found! Stopping!");
					running = false;
					return;
				} catch (com.rabbitmq.client.AuthenticationFailureException afe) {
					parent.log(MessageType.SEVERE, "Incorrect username/password for RabbitMQ. Stopping...");
					running = false;
					return;
				}
			}
			parent.log(MessageType.INFO, "Connected to RabbitMQ server.");
			connection.addShutdownListener(new ShutdownListener() {

				@Override
				public void shutdownCompleted(ShutdownSignalException arg0) {
					parent.log(MessageType.WARNING, "No connection to RabbitMQ server, retrying every 5 seconds...");
				}
				
			});
			connection.addRecoveryListener(new RecoveryListener() {

				@Override
				public void handleRecovery(Recoverable arg0) {
					parent.log(MessageType.INFO, "Connection recovered!");
				}

				@Override
				public void handleRecoveryStarted(Recoverable arg0) {
					parent.log(MessageType.INFO, "Starting automatic connection recovery...");
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
				channel.basicPublish("fietsenrek_servers", "", null, ("[\"heartbeat\", \""+parent.getModel().getName()+"\", "+parent.getModel().getSlotDataJson()+", \""+ip+"\"]").getBytes());
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
