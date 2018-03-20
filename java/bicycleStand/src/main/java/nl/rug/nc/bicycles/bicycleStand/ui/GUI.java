package nl.rug.nc.bicycles.bicycleStand.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import nl.rug.nc.bicycles.bicycleStand.HeartbeatRunnable;
import nl.rug.nc.bicycles.bicycleStand.model.StandData;

public class GUI extends UI implements ActionListener, Observer {
	
	private JTextField nameField = new JTextField(20);
	private JTextField totalField = new JTextField("0");
	private JTextField toggleField = new JTextField();
	private JButton toggleButton = new JButton("Toggle");
	private JLabel freeLabel = new JLabel("0");
	private JProgressBar freeSpotBar = new JProgressBar();
	private JFrame guiFrame = new JFrame();
	
	public GUI() {
		setLogLevel(MessageType.WARNING);
		guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		guiFrame.setContentPane(content);
		guiFrame.getContentPane().setLayout(new BorderLayout());
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout());
		upperPanel.add(nameField, BorderLayout.CENTER);
		guiFrame.getContentPane().add(upperPanel, BorderLayout.NORTH);
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridLayout(2,0));
		JPanel slotsPanel = new JPanel(new GridLayout(2,0));
		slotsPanel.add(new JLabel("Free slots: "));
		slotsPanel.add(freeLabel);
		slotsPanel.add(new JLabel("Total: "));
		slotsPanel.add(totalField);
		centerPanel.add(slotsPanel);
		centerPanel.add(freeSpotBar);
		guiFrame.getContentPane().add(centerPanel, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(0,3));
		bottomPanel.add(new JLabel("Set: "));
		bottomPanel.add(toggleField);
		bottomPanel.add(toggleButton);
		toggleField.addActionListener(this);
		toggleButton.addActionListener(this);
		guiFrame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		guiFrame.pack();
		guiFrame.setResizable(false);
		guiFrame.setVisible(true);
	}
	
	public boolean validateForm() {
		try {
			return (Integer.valueOf(toggleField.getText()) < Integer.valueOf(totalField.getText())
					&& Integer.valueOf(toggleField.getText()) >= 0);
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	private String[] showConnectionDialog() {
		JTextField ip = new JTextField(20), user= new JTextField(20);
		JPasswordField pass = new JPasswordField(20);
		Object[] message = {
				"IP:", ip,
				"Username:", user,
				"Password:", pass
		};
		int result = JOptionPane.showConfirmDialog(guiFrame, message, "Connection info...", JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			return new String[] {user.getText(), String.valueOf(pass.getPassword()), ip.getText()};
		}
		return null;
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		if (!validateForm()) {
			JOptionPane.showMessageDialog(guiFrame, "Invalid value", "Error: invalid value", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (this.getModel()==null) {
			String[] connectionInfo = showConnectionDialog();
			if (connectionInfo==null) return;
			int max = Integer.valueOf(totalField.getText());
			StandData data = new StandData(nameField.getText(), max);
			data.addObserver(this);
			setModel(data);
			freeSpotBar.setMaximum(max);
			totalField.setEnabled(false);
			nameField.setEnabled(false);
			new Thread(new HeartbeatRunnable(this, connectionInfo)).start();
		}
		int slot = Integer.valueOf(toggleField.getText());
		getModel().toggleSlot(slot);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		freeSpotBar.setValue(getModel().getFilledSlotCount());
		freeLabel.setText(""+getModel().getFreeSlotCount());
	}

	@Override
	public void showMessage(MessageType type, String message) {
		JOptionPane.showMessageDialog(guiFrame, message, "", type.getJOptionPaneMessageType());
	}

}
