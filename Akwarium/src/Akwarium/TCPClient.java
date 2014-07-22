package Akwarium;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

import javax.swing.JOptionPane;

public class TCPClient extends Connection implements Runnable {
	
	private byte[] buffer = new byte[64];
	private static int port = 4945;
	private boolean isConnected;
	private boolean disconnected;
	private boolean isServerUp;
	private boolean serverDown;
	private InetAddress IPAddress;    // address of server
	private UDPClient recCoor;
	private Thread t;
	
	
	TCPClient (InetAddress IPAddress) {
		
		this.IPAddress = IPAddress;
	}
	
	public void run () {
		
		
		ServerSocket server = null;
		Socket client = null;
		InputStream in = null;
		OutputStream out = null;
		int retries = 0;
		
		
		int time = (int)System.currentTimeMillis();
		buffer[0] = (byte)0x80;
		buffer[1] = (byte)time;  // Hello message xD
		buffer[2] = (byte)(time >>> 8);
		buffer[3] = (byte)(time >>> 16);
		buffer[4] = (byte)(time >>> 24);
		
		try {
			
			client = new Socket();
			client.connect(new InetSocketAddress(IPAddress, port), 0); // socket.accept()
			isServerUp = true;
			out = client.getOutputStream();
			out.write(buffer, 0, 5);   // send hello message
			in = client.getInputStream();
			int op;
			while ((op = in.read()) != -1) {
				
				switch(PacketInterpreter.interpret(op, in)) {
				case 1:
					buffer[0] = (byte)0xFE;   // iv resolved
					buffer[1] = (byte)0x00;
					out.write(buffer, 0, 2);
					recCoor = new UDPClient(port);
					recCoor.startThread();
					isConnected = true;
					break;
				case 0:
					buffer[0] = (byte)0x00;
					buffer[1] = (byte)0x00;
					out.write(buffer, 0, 2);  // OK
					retries = 0;
					break;
				case -1:
					buffer[0] = (byte)0xFF;
					buffer[1] = (byte)0x00;
					out.write(buffer, 0, 2);  // FAILED
					retries++;
					if(retries > 4) {
						System.out.println("DESYNCHRONIZATION");
						isConnected = false;
						client.close();
						recCoor.terminate();
						return;
					}
					break;
				}
			}
			
		} catch (IOException e) {
			if(e.getClass() ==  SocketException.class) {
				System.out.println("Server disconnected");
				disconnected = true;
				isConnected = false;
			} else if(e.getClass() ==  ConnectException.class) {
				System.out.println("Cannot connect to server");
				serverDown = true;
			} else if(e.getClass() ==  BindException.class) {
				System.out.println("Cannot bind to port " + port);
				JOptionPane.showMessageDialog(null, 
						"Cannot bind to port " + port,
					    "Bind error",
					    JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			} else if(e.getClass() == NoRouteToHostException.class) {
				System.out.println("Internet connection problem");
				serverDown = true;
			} else {
				System.out.println("Cannot read/send data to server");
				e.printStackTrace();
				System.exit(-1);
			}
			
		}
		
		
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Cannot close socket");
			return;
		}
		
		if(recCoor != null)
			recCoor.terminate();
	}
	
	
	public void startThread () {
		
		t = new Thread(this);
		t.start();
	}
	
	public boolean isConnected () {
		
		return isConnected;
	}
	
	public boolean isServerUp () {
		
		return isServerUp;
	}
	
	public boolean isDisconnected () {
		
		return disconnected;
	}
	
	public boolean getServerDown () {
		
		return serverDown;
	}

	public InetAddress getIPAddress() {
		
		return IPAddress;
	}
	
	public int getNextPort () {
		
		port++;
		return port;
	}

}
