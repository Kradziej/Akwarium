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

	static boolean isMultiplayer;
	static boolean isClient;
	static boolean isServer;
	static PipedOutputStream tcpOutput;
	static PipedInputStream tcpInput;
	static PipedOutputStream udpOutput;
	static PipedInputStream udpInput;
	//static TCPServer server;
	//static TCPClient client;
	static Connection con;
	static UDPServer playerOut;
	static UDPClient playerIn;
	static IPaddressPopup addr = new IPaddressPopup();
	
	public static void main(String[] args) {
		
		
		while(true) {
			
			// Choose mode
			addr.setModal(true);
			addr.setVisible(true);
			InetAddress ip = null;
			try {
				//ip = InetAddress.getByName(addr.getIpAddress());
				ip = InetAddress.getByName("localhost");
			} catch (UnknownHostException e) {
				JOptionPane.showMessageDialog(null, 
						"Incorrect IP address.",
					    "Connection error",
					    JOptionPane.ERROR_MESSAGE);
				continue;
			}

			isServer = addr.isServer();
			isClient = addr.isClient();
			isMultiplayer = isClient | isServer;
			
			// Load images
			Animal.loadResources();
								
		
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
					con = new TCPServer(tcpInput, udpInput);  // sender
					con.startThread();
				}
				
				if(isClient) {
					con = new TCPClient(ip);    //receiver
					con.startThread();
					// first check if server is up, if not then back to the beginning
					if(checkServerUp()) {
						JOptionPane.showMessageDialog(null, 
								"Cannot connect to server.",
							    "Connection error",
							    JOptionPane.ERROR_MESSAGE);
						continue;
					}
				}
			
			}
			
			// Create Window
			JFrame frame = DrawAq.createFrame("Aquarium");
			JPanel cPanel = DrawAq.createPanel(new BorderLayout());
			JPanel bPanel = DrawAq.createPanel(new GridLayout(0,4));
			JPanel tPanel = DrawAq.createPanel(new BorderLayout());
			
			// Console
			frame.add(tPanel, BorderLayout.PAGE_START);
			JTextArea console = new JTextArea();
			console.setEditable(false);
			tPanel.add(console);
			
			
			// Create aquarium
			Aquarium aquarium = new Aquarium(new Filter(), new Lamp(), 5000, console, isServer, isClient);
			PacketInterpreter.setAq(aquarium);
						
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
			
			
			
			if(isMultiplayer)
				if(waitConnection())  // something failed on the way to establish connection
					continue;
			
			
			// Create init animals
			if(isServer) {
				aquarium.initAnimalsServer();
				aquarium.initSharksServer();   
				playerIn = new UDPClient(con.getNextPort());
				playerIn.startThread();
				
			} else if(isClient) {
				playerOut = new UDPServer(udpInput, con.getIPAddress(), con.getNextPort());
				playerOut.startThread();
			} else {
				aquarium.initAnimalsServer();
				aquarium.initSharksServer();  
			}
		
			//waitresponse isGraphicsReady
			
			float boost = 1.00f;
			int SYNCH_TIME = 40;
			
			while(con.isConnected()) {
	
				aquarium.updateData();
				
				if(isServer || !isMultiplayer) {
					
					boost += 0.00001f;
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
					
				}
				
				
				
				try {
					Thread.sleep(SYNCH_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
				
			}
			
			// Cleaning
			aquarium.killAllAnimals();
			
			String msg = null;
			if(isServer)
				msg = "Player disconnected.";
			if(isClient)
				msg = "Server disconnected.";
			
			JOptionPane.showMessageDialog(null, msg, "Connection error", JOptionPane.WARNING_MESSAGE);
			
			try {
				tcpOutput.close();
				udpOutput.close();
				tcpInput.close();
				udpInput.close();
			} catch (IOException e) {
				System.out.println("Cannot close pipes");
				e.printStackTrace();
				System.exit(-1);
			}
			
			con = null;
			playerOut = null;
			if(isServer) {
				playerIn.terminate();
				playerIn = null;
			}
			aquarium = null;
			frame.dispose();
			frame = null;
			
		
		}
		
					
	}
	
	
	
	public static boolean waitConnection () {
		
		while(!con.isConnected()) {
			
			if(con.isDisconnected())
				return true;
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		return false;
	}
	
	public static boolean checkServerUp () {
		
		while(!con.isServerUp()) {
			
			if(con.getServerDown()) {
				return true;
			}
			
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		return false;
	}


	public static void createPet (Aquarium Aq) {
		
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
