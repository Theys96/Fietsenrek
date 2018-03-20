package nl.rug.nc.bicycles.bicycleStand.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import nl.rug.nc.bicycles.bicycleStand.HeartbeatRunnable;
import nl.rug.nc.bicycles.bicycleStand.model.StandData;

public class GUI extends JFrame implements ActionListener, Observer {
	
	private static final long serialVersionUID = -8263279312626846365L;
	private JTextField nameField = new JTextField(20);
	private JTextField totalField = new JTextField("0");
	private JTextField toggleField = new JTextField();
	private JButton toggleButton = new JButton("Toggle");
	private JLabel freeLabel = new JLabel("0");
	private JProgressBar freeSpotBar = new JProgressBar();
	private StandData data = null;
	
	public GUI() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().setLayout(new BorderLayout());
		JPanel upperHalf = new JPanel();
		upperHalf.setLayout(new BorderLayout());
		upperHalf.add(nameField, BorderLayout.CENTER);
		this.getContentPane().add(upperHalf, BorderLayout.NORTH);
		JPanel lowerHalf = new JPanel();
		lowerHalf.setLayout(new GridLayout(2,0));
		JPanel slotsPanel = new JPanel(new GridLayout(2,0));
		slotsPanel.add(new JLabel("Free slots: "));
		slotsPanel.add(freeLabel);
		slotsPanel.add(new JLabel("Total: "));
		slotsPanel.add(totalField);
		lowerHalf.add(slotsPanel);
		lowerHalf.add(freeSpotBar);
		this.getContentPane().add(lowerHalf, BorderLayout.CENTER);
		JPanel bottomHalf = new JPanel();
		bottomHalf.setLayout(new GridLayout(0,3));
		bottomHalf.add(new JLabel("Set "));
		bottomHalf.add(toggleField);
		bottomHalf.add(toggleButton);
		toggleField.addActionListener(this);
		toggleButton.addActionListener(this);
		this.getContentPane().add(bottomHalf, BorderLayout.SOUTH);
		this.pack();
		this.setResizable(false);
		this.setVisible(true);
	}
	
	public boolean validateForm() {
		try {
			return (Integer.valueOf(toggleField.getText()) < Integer.valueOf(totalField.getText())
					&& Integer.valueOf(toggleField.getText()) >= 0);
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public StandData getModel() {
		return data;
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		if (!validateForm()) {
			JOptionPane.showMessageDialog(this, "Invalid value", "Error: invalid value", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (data==null) {
			int max = Integer.valueOf(totalField.getText());
			data = new StandData(nameField.getText(), max);
			data.addObserver(this);
			freeSpotBar.setMaximum(max);
			totalField.setEnabled(false);
			nameField.setEnabled(false);
			new Thread(new HeartbeatRunnable(this)).start();
		}
		int slot = Integer.valueOf(toggleField.getText());
		data.toggleSlot(slot);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		freeSpotBar.setValue(data.getFilledSlotCount());
		freeLabel.setText(""+data.getFreeSlotCount());
	}

}
