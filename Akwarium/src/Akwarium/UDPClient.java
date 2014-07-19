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

public class UDPClient extends PacketInterpreter implements Runnable {
	
	private byte[] buffer = new byte[256];
	private int port;
	private boolean runServer = true;
	private int iv;
	DatagramSocket socket;
	private InputStream in;
	private Thread t;
	
	
	UDPClient (int iv, int port) {
		
		this.port = port;
		this.iv = iv;
	}
	
	public void run () {
		
		
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
			System.out.println("Cannot create socket on port " + port);
			System.exit(-1);
		}
		
		
		DatagramPacket rPacket = new DatagramPacket(buffer, buffer.length);
		in = new ByteArrayInputStream(buffer);
		int op;
		
		while(runServer) {
			try {
				socket.receive(rPacket);   // start listening for coordinates
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Failed to read data from socket");
				System.exit(-1);
			}
			
		
			// Analyse packet
			try {
				//while((op = in.read()) == 1) {
				interpret(in.read(), in);  // update coordinates
				//}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Cannot read/send data to server");
				System.exit(-1);
			}
		
			
			try {
				in.reset();    // reset buffer
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Cannot reset input");
				System.exit(-1);
			}
		}
		
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
