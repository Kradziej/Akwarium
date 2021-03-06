package akwarium;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import packet.PacketInterpreter__;
import packet.PacketSender;
import connection.Connection;
import connection.TCPClient;
import connection.TCPServer;
import connection.UDPClient;
import connection.UDPServer;

public class Program {

	static boolean isMultiplayer;
	static boolean isClient;
	static boolean isServer;
	static PipedOutputStream tcpOutput;
	static PipedInputStream tcpInput;
	static PipedOutputStream udpOutput;
	static PipedInputStream udpInput;
	static Connection con;
	static UDPServer playerOut;
	static UDPClient playerIn;
	static PacketSender packetSenderThread;
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
					PacketSender.initSender(tcpOutput, udpOutput);
					packetSenderThread = new PacketSender();
					packetSenderThread.start();
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
			//JPanel bPanel = DrawAq.createPanel(new GridLayout(0,4));

			// Add status panel
			JPanel sPanel = new StatusPanel(isMultiplayer);
			frame.add(sPanel, BorderLayout.PAGE_START);
			sPanel.setPreferredSize(new Dimension(0, (int)(24 * DrawAq.yAnimalScale())));


			// Create aquarium
			Aquarium aquarium = Aquarium.getInstance(isServer, isClient, (StatusPanel)sPanel);
			PacketInterpreter__.setAq(aquarium);


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
			frame.pack();
			frame.repaint();



			if(isMultiplayer)
				if(waitConnection())  // something failed on the way to establish connection
					continue;


			// Create init animals
			if(isServer) {
				aquarium.initAnimals();
				aquarium.initSharks();
				Mine.initResources();
				playerIn = new UDPClient(con.getNextPort());
				playerIn.startThread();

			} else if(isClient) {
				aquarium.initSharks();
				Mine.initResources();
				playerOut = new UDPServer(udpInput, con.getIPAddress(), con.getNextPort());
				playerOut.startThread();
			} else {
				aquarium.initAnimals();
				aquarium.initSharks();
				Mine.initResources();
			}

			if(isMultiplayer)
				waitGraphicsReady();

			float base = 1f;
			float boost;
			double boostPow;
			int animalMaxNumber = 10;
			float exponent = 0.2f;
			int SYNCH_TIME = 40;

			while(!isMultiplayer || con.isConnected()) {

				aquarium.updateData();

				if(isServer || !isMultiplayer) {

					base += 0.01f;
					//aquarium.increaseShift((float)(0.015*Math.pow(boost, 2)));
					boostPow = Math.pow(base, exponent);
					boost = (float)(boostPow  *  Math.log(boostPow));
					aquarium.setBoost(boost);
					int allAnimals = aquarium.getNumberOfAnimals();
					Random rand = new Random();
					int p = rand.nextInt(100);
					float boostInc = boost/3;


					if (p < 1 && allAnimals < animalMaxNumber + boostInc + 9) {
						createCustomPet(aquarium, new Mine(aquarium));
					} else if (p < 10 && allAnimals < animalMaxNumber + boostInc + 8) {
						createPet(aquarium);
					} else if (p < 20 && allAnimals < animalMaxNumber + boostInc + 4) {
						createPet(aquarium);
					} else if (p < 40 && allAnimals < animalMaxNumber + boostInc) {
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

			// terminate sender
			PacketSender.terminate();
			packetSenderThread = null;

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


			JOptionPane.showMessageDialog(null, msg, "Connection error", JOptionPane.WARNING_MESSAGE);

		}


	}


	public static boolean waitGraphicsReady () {

		while(!con.isGraphicsReady()) {

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}

		return true;
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


	public static void createPet (Aquarium aq) {

		try {
			aq.addAnimal();
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			System.out.println("Cannot create pet");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void createCustomPet (Aquarium aq, Animal a) {

		try {
			aq.addAnimal(a);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			System.out.println("Cannot create pet");
			e.printStackTrace();
			System.exit(-1);
		}
	}




}
