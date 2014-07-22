package Akwarium;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.InetAddress;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

public class IPaddressPopup extends JDialog implements ActionListener {
	
	private JButton multi = new JButton("Multiplayer");
	private JButton single = new JButton("Singleplayer");
	private JTextField ipText = new JTextField("IP address");
	private JRadioButton client = new JRadioButton("Client");
	private JRadioButton server = new JRadioButton("Server");
	private JPanel panelText = new JPanel(new BorderLayout());
	private JPanel buttons = new JPanel(new FlowLayout());
	private JPanel radioButtons = new JPanel(new FlowLayout());
	
	IPaddressPopup () {
		
		this.setTitle("Akwarium");
		ipText.selectAll();
		panelText.add(ipText);
		buttons.add(multi);
		buttons.add(single);
		radioButtons.add(server);
		radioButtons.add(client);
		buttons.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
		panelText.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
		this.add(panelText, BorderLayout.PAGE_START);
		this.add(radioButtons, BorderLayout.CENTER);
		this.add(buttons, BorderLayout.PAGE_END);
		multi.addActionListener(this);
		single.addActionListener(this);
		client.addActionListener(this);
		server.addActionListener(this);
		//exit on X clicked
		this.addWindowListener(new WindowAdapter() {
		
			@Override
		    public void windowClosing(WindowEvent e) {
		        System.exit(0);
		    }
		});
		
		server.setSelected(true);
		this.invalidate();
		this.pack();
		this.setLocationRelativeTo(null);
		this.setResizable(false);
	}
	
	

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource().equals(multi)) {
			
			this.setVisible(false);
		}
		
		if(e.getSource().equals(single)) {
			
			client.setSelected(false);
			server.setSelected(false);
			this.setVisible(false);
		}
		
		if(e.getSource().equals(server)) {
			
			server.setSelected(true);
			client.setSelected(false);
		}
		
		if(e.getSource().equals(client)) {
			
			client.setSelected(true);
			server.setSelected(false);
			
		}
	}
	
	
	
	public boolean isServer () {
		
		return server.isSelected();
	}
	
	public boolean isClient () {
		
		return client.isSelected();
	}
	
	public String getIpAddress () {
		
		return ipText.getText();
	}
	
	
	
}
