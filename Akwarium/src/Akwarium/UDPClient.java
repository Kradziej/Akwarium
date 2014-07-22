package Akwarium;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.swing.JOptionPane;

public class UDPClient extends PacketInterpreter implements Runnable {
	
	private byte[] buffer = new byte[64];
	private int port;
	private boolean runServer = true;
	private int iv;
	DatagramSocket socket;
	private InputStream in;
	private Thread t;
	
	
	UDPClient (int port) {
		
		this.port = port;
	}
	
	public void run () {
		
		
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.out.println("Cannot create socket on port " + port);
			JOptionPane.showMessageDialog(null, 
					"Cannot bind to port " + port,
				    "Bind error",
				    JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		
		
		DatagramPacket rPacket = new DatagramPacket(buffer, buffer.length);
		in = new ByteArrayInputStream(buffer);
		int op;
		
		while(runServer) {
			try {
				socket.receive(rPacket);   // start listening for coordinates
			} catch (IOException e) {
				if(e.getClass() ==  SocketException.class) {
					System.out.println("Server disconnected");
					break;
				} else {
					e.printStackTrace();
					System.out.println("Failed to read data from socket");
					System.exit(-1);
				}
			}
			
		
			// Analyse packet
			try {
				interpret(in.read(), in);  // update coordinates
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Cannot read data from input stream");
				break;
			}
		
			
			try {
				in.reset();    // reset buffer
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Cannot reset input stream");
				break;
			}
		}
		
		socket.close();
		
	}	
	
	
	
	public void startThread () {
		
		t = new Thread(this);
		t.start();
	}

	public void terminate () {
		
		socket.close();
		runServer = false;
	}

}
