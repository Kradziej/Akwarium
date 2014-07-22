package Akwarium;

import java.io.IOException;
import java.io.PipedInputStream;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

public class UDPServer implements Runnable, PacketConstants {
	
	private byte[] buffer = new byte[64];
	private int port;
	private InetAddress IPAddress;
	private int iv;
	private static PipedInputStream packetInput;
	Thread t;
	
	UDPServer (PipedInputStream in, InetAddress IPAddress, int port) {
		
		this.port = port;
		this.IPAddress = IPAddress;
		packetInput = in;
	}
	
	
	public void run () {
		
		DatagramSocket socket = null;
		DatagramPacket dPacket = null;
		
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			if(e.getClass() ==  BindException.class) {
				System.out.println("Cannot create socket on port " + port);
				JOptionPane.showMessageDialog(null, 
						"Cannot bind to port " + port,
					    "Bind error",
					    JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
		}
		
		
		try {
			
			int op;
			int bytesRead;
			while ((op = packetInput.read()) != -1) {
				
				bytesRead = packet.getSize(op);
				buffer[0] = (byte)op;
				packetInput.read(buffer, 1, bytesRead);
				dPacket = new DatagramPacket(buffer, bytesRead+1, IPAddress, port);
				socket.send(dPacket);
			}
			
		} catch (IOException e) {
		
			System.out.println("Cannot read data from input stream");
		}
		
		socket.close();
			
	}
	
	
	public void startThread () {
		
		t = new Thread(this);
		t.start();
	}

}
