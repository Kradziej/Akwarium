package connection;

import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JOptionPane;

import packet.PacketInterpreter;
import packet.ResponseHandler;
import akwarium.Connection;
import akwarium.DrawAq;

public class TCPClient extends Connection implements Runnable {

	private byte[] buffer = new byte[128];
	private int port = 4945;
	private boolean isConnected;
	private boolean disconnected;
	private boolean isGraphicsReady;
	private boolean isServerUp;
	private boolean serverDown;
	private InetAddress IPAddress;    // address of server
	private UDPClient recCoor;
	private ResponseHandler rHandler;
	private Thread t;

	TCPClient () {

		port = 4945;
		rHandler = new ResponseHandler();
	}

	TCPClient (InetAddress IPAddress) {

		this();
		this.IPAddress = IPAddress;
	}

	@Override
	public void run () {

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

			// send hello message
			out.write(buffer, 0, 5);
			
			// send settings
			Dimension d = DrawAq.getResolution();
			buffer[0] = (byte)0x81;
			int w = (int)d.getWidth();
			buffer[1] = (byte) w;
			buffer[2] = (byte)(w >>> 8);
			buffer[3] = (byte)(w >>> 16);
			buffer[4] = (byte)(w >>> 24);
			int h = (int)d.getHeight();
			buffer[5] = (byte) h;
			buffer[6] = (byte)(h >>> 8);
			buffer[7] = (byte)(h >>> 16);
			buffer[8] = (byte)(h >>> 24);
			out.write(buffer, 0, 9);

			// Run listening
			in = client.getInputStream();
			int op;
			DataInputStream inBuff = new DataInputStream(in);
			while (true) {

				op = inBuff.readShort();
				switch(PacketInterpreter.interpret(op, inBuff)) {

				case 0:					     // standard OK response
					buffer[0] = (byte)0x00;
					buffer[1] = (byte)0x00;
					out.write(buffer, 0, 2);
					retries = 0;
					break;

				case -1:					 // FAILED
					buffer[0] = (byte)0xFF;
					buffer[1] = (byte)0x00;
					out.write(buffer, 0, 2);
					retries++;
					if(retries > 4) {
						System.out.println("DESYNCHRONIZATION");
						isConnected = false;
						client.close();
						recCoor.terminate();
						return;
					}
					break;

				case 1:
					buffer[0] = (byte)0xFE;   // iv resolved
					buffer[1] = (byte)0x00;
					out.write(buffer, 0, 2);
					recCoor = new UDPClient(port);
					recCoor.startThread();
					isConnected = true;
					break;

				case 2:   					  // images loaded
					buffer[0] = (byte)0xFD;
					buffer[1] = (byte)0x00;
					out.write(buffer, 0, 2);
					isGraphicsReady = true;
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
			} else if(e.getClass() == EOFException.class) {
				System.out.println("Server disconnected");
				disconnected = true;
				isConnected = false;
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


	@Override
	public void startThread () {

		t = new Thread(this);
		t.start();
	}

	@Override
	public boolean isConnected () {

		return isConnected;
	}

	@Override
	public boolean isServerUp () {

		return isServerUp;
	}

	@Override
	public boolean isDisconnected () {

		return disconnected;
	}

	@Override
	public boolean getServerDown () {

		return serverDown;
	}

	@Override
	public boolean isGraphicsReady () {

		return isGraphicsReady;
	}


	@Override
	public InetAddress getIPAddress() {

		return IPAddress;
	}

	@Override
	public int getNextPort () {

		port++;
		return port;
	}

}
