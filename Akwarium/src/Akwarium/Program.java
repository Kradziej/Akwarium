package Akwarium;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Program {

	public static void main(String[] args) {
		
		// Create Window
		JFrame frame = DrawAq.createFrame("Aquarium");
		JPanel cPanel = DrawAq.createPanel(new BorderLayout());
		JPanel bPanel = DrawAq.createPanel(new GridLayout(0,4));
		JPanel tPanel = DrawAq.createPanel(new BorderLayout());
		//bPanel.setPreferredSize(new Dimension(100,100));
		
		// Console
		frame.add(tPanel, BorderLayout.PAGE_END);
		JTextArea console = new JTextArea();
		console.setEditable(false);
		tPanel.add(console);
		
		
		// Choose mode
		IPaddressPopup addr = new IPaddressPopup();
		addr.setModal(true);
		addr.setVisible(true);
		InetAddress ip = null;
		try {
			//ip = InetAddress.getByName(addr.getIpAddress());
			ip = InetAddress.getByName("192.168.5.100");
		} catch (UnknownHostException e) {
			System.out.println("Host not found");
			e.printStackTrace();
			System.exit(-1);
		}
		//System.out.println(ip);
		//System.exit(0);
		boolean isServer = addr.isServer();
		boolean isClient = addr.isClient();
		
		// Create server, client and piped streams
		boolean isMultiplayer = isClient | isServer;
		PipedOutputStream tcpOutput = null;
		PipedInputStream tcpInput = null;
		PipedOutputStream udpOutput = null;
		PipedInputStream udpInput = null;
		TCPServer server = null;
		TCPClient client = null;
		UDPServer playerOut = null;
		UDPClient playerIn = null;
		
		// Create aquarium
		Aquarium aquarium = new Aquarium(new Filter(), new Lamp(), 5000, console, isServer, isClient);
		PacketInterpreter.setAq(aquarium);
		
		
		// Initialize multiplayer if checked
		if(isMultiplayer) {
			
			try {
				udpOutput = new PipedOutputStream();
				udpInput = new PipedInputStream(udpOutput);
				tcpOutput = new PipedOutputStream();
				tcpInput = new PipedInputStream(tcpOutput);
				PacketSender.setOutputs(tcpOutput, udpOutput);
			} catch (IOException e) {
				System.out.println("Cannot create pipes");
				e.printStackTrace();
				System.exit(-1);
			}
			
			if(isServer) {
				server = new TCPServer(tcpInput, udpInput);  // sender
				server.startThread();
			}
			if(isClient) {
				client = new TCPClient(ip);    //receiver
				client.startThread();
			}
		
			
			// Wait for connection
			if(isServer) {
				
				while(!server.isConnected()) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				}
					
			}
			
			if(isClient) {
			
				while(!client.isConnected()) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				}
			}
		}
			
		
	
		/*
		// buttons
		frame.add(bPanel, BorderLayout.PAGE_START);
		JButton addAnimalButton = new myButtonAdd(aquarium, "Add new pet");
		JButton removeAnimalButton = new myButtonDelete(aquarium, "Remove pet");
		JButton ColorPicker = new myColorPicker(aquarium, "Color picker");
		JButton addShark = new myButtonAddShark(aquarium, "Shark!");
		bPanel.add(addAnimalButton);
		bPanel.add(removeAnimalButton);
		bPanel.add(ColorPicker);
		bPanel.add(addShark);
		*/
		
		// Set initial focus on canvas
		aquarium.getCanvas().setFocusable(true);
		aquarium.getCanvas().requestFocusInWindow();
		
		// Window names
		if(isServer)
			frame.setTitle(frame.getTitle()+" Server");
		else if(isClient)
			frame.setTitle(frame.getTitle()+" Client");
		
		// Canvas Panel
		frame.add(cPanel, BorderLayout.CENTER);
		cPanel.add(aquarium.getCanvas());
		frame.setVisible(true);
		frame.invalidate();
		frame.repaint();
		
		
		// Create init animals
		if(isServer) {
			aquarium.initAnimalsServer();
			aquarium.initSharksServer();   
			playerIn = new UDPClient(server.getNextPort());
			playerIn.startThread();
			
		} else if(isClient) {
			playerOut = new UDPServer(udpInput, client.getIPAddress(), client.getNextPort());
			playerOut.startThread();
		}
	
		
		
		float boost = 1.00f;
		
		while(true) {

			aquarium.updateData();
			
			if(isServer) {
				
				boost += 0.0001f;
				aquarium.increaseShift((float)(0.1*Math.pow(boost, 2)));
				int allAnimals = aquarium.getNumberOfAnimals();
				Random rand = new Random();
				int p = rand.nextInt(100);
				
				if (p < 40 && allAnimals < 7) {
					createPet(aquarium);
				} else if (p < 20 && allAnimals < 8) {
					createPet(aquarium);
				} else if (p < 10 && allAnimals < 10) {
					createPet(aquarium);
				}
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
				
			} else if(isClient) {
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			}
				
			
			
		}
		
					
	}
	
	
	public static void createPet(Aquarium Aq) {
		
		try {
			Aq.addAnimal();
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			System.out.println("Cannot create pet");
			e.printStackTrace();
			System.exit(-1);	
		}
	}
	
	
	

}
