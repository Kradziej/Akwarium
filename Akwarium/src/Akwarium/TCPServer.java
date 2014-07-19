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

public class TCPServer extends packetSender implements Runnable {
	
	private static final int PORT = 4945;
	private byte[] bufferIn = new byte[64];
	private byte[] bufferOut = new byte[64];
	private InetAddress IPAddress;
	private int iv;
	private UDPServer sendCoor;
	private boolean isConnected;
	Thread t;
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
			server = new ServerSocket(PORT);
			client = server.accept();
			in = client.getInputStream();
			while (in.read(bufferIn) != -1) {
				if((byte)(bufferIn[0] ^ 0x80) == 0) {	 	  // get hello message
					out = client.getOutputStream();
					packetSender.sendIv();   // send iv
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
					 isConnected = true;
					 sendCoor = new UDPServer(udpInput, client.getInetAddress(), PORT+1);
					 sendCoor.startThread();
				}
			}
		
		} catch (IOException e) {
			if(e.getClass() ==  ConnectException.class)
				System.out.println("Cannot connect to client");
			if(e.getClass() ==  BindException.class)
				System.out.println("Cannot bind to port " + PORT);
			else
				System.out.println("Cannot read data from client");
				
			e.printStackTrace();
			System.exit(-1);
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
	


}
