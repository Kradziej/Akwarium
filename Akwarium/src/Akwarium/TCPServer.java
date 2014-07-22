package Akwarium;

import java.awt.Color;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

import javax.swing.JOptionPane;

public class TCPServer extends Connection implements Runnable, PacketConstants {
	
	private byte[] bufferIn = new byte[64];
	private byte[] bufferOut = new byte[64];
	private UDPServer sendCoor;
	private boolean isConnected;
	private boolean disconnected;
	private static int port = 4945;
	Thread t;
	private InetAddress IPAddress;     // server client is connected with
	private PipedInputStream tcpInput;
	private PipedInputStream udpInput;
	
	TCPServer (PipedInputStream tcp, PipedInputStream udp) {
		
		tcpInput = tcp;
		udpInput = udp;
		
	}
	
	public void run () {
		
		
		ServerSocket server = null;
		Socket client = null;
		InputStream in = null;
		OutputStream out = null;
		int retries = 0;
		
		
		try {
			server = new ServerSocket(port);
			server.setSoTimeout(0);
			client = server.accept();
			IPAddress = client.getInetAddress();
			in = client.getInputStream();
			while (in.read(bufferIn) != -1) {
				if((byte)(bufferIn[0] ^ 0x80) == 0) {	 	  // get hello message
					out = client.getOutputStream();
					PacketSender.sendIv();   // send iv
					break;
				}
			}
			
			int op;
			int bytesToRead;
			while ((op = tcpInput.read()) != -1) {
				bytesToRead = packet.getSize(op);
				bufferOut[0] = (byte)op;
				tcpInput.read(bufferOut, 1, bytesToRead);
				out.write(bufferOut, 0, bytesToRead+1);
				in.read(bufferIn);
				if(bufferIn[0] == 0x00) {			//OK
					retries = 0;
				} else if((byte)(bufferIn[0] ^ 0xFF) == 0) {    //FAILED
					retries++;
					if(retries > 4) {
						System.out.println("DESYNCHRONIZATION");
						isConnected = false;
						client.close();
						server.close();
						return;
					} 		
				} else if((byte)(bufferIn[0] ^ 0xFE) == 0) {   // iv ready
					 sendCoor = new UDPServer(udpInput, client.getInetAddress(), port);
					 sendCoor.startThread();
					 isConnected = true;
				}
			}
		
		} catch (IOException e) {
			if(e.getClass() ==  SocketException.class) {
				System.out.println("Client disconnected");
				disconnected = true;
				isConnected = false;
			} else if(e.getClass() ==  BindException.class) {
				System.out.println("Cannot bind to port " + port);
				JOptionPane.showMessageDialog(null, 
						"Cannot bind to port " + port,
					    "Bind error",
					    JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			} else {
				System.out.println("Cannot read data from client");
				e.printStackTrace();
				System.exit(-1);
			}
				
		}
		
		
		try {
			client.close();
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Cannot close socket");
			System.exit(-1);
		}
		
		
	}
	
	
	public void startThread () {
		
		t = new Thread(this);
		t.start();
	}
	
	public boolean isConnected () {
		
		return isConnected;
	}
	
	public boolean isDisconnected () {
		
		return disconnected;
	}

	public InetAddress getIPAddress () {
		
		return IPAddress;
	}
	
	public int getNextPort () {
		
		port++;
		return port;
	}

}
