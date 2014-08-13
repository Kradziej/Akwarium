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

import packet.PacketHandler;
import packet.PacketSender;
import static packet.PacketConstants.Packet;
import akwarium.DrawAq;

public class TCPClient extends Connection implements Runnable {

	private boolean isConnected;
	private boolean isGraphicsReady;
	private boolean isServerUp;
	private InetAddress IPAddress;    // address of server
	private UDPClient recCoor;
	Socket client;
	private Thread t;

	TCPClient () {

		port = 4945;
	}

	TCPClient (InetAddress IPAddress) {

		this();
		this.IPAddress = IPAddress;
	}

	@Override
	public void run () {

		InputStream in = null;
		OutputStream out = null;
		short op;
		PacketSender sender = PacketSender.getSender();

		try {

			client = new Socket();
			client.connect(new InetSocketAddress(IPAddress, port), 0); // socket.accept()
			isServerUp = true;
			// init client output
			out = client.getOutputStream();
			sender.addTcpOutput(out);
			
			// send hello message
			int time = (int)System.currentTimeMillis();
			sender.sendData(Packet.HELLO_MESSAGE, time);
			try {
				Thread.currentThread().wait();
			} catch (InterruptedException e) {
				System.out.println(this.toString() + " thread interrupted!");
				client.close();
				return;
			}
			
			// send settings
			Dimension d = DrawAq.getResolution();
			sender.sendData(Packet.SETTINGS, d.getWidth(), d.getHeight());
			
			// Run listening
			in = client.getInputStream();
			DataInputStream inBuff = new DataInputStream(in);
			
			while (true) {

				op = inBuff.readShort();
				PacketHandler.interpret(op, inBuff);
			}

		} catch (IOException e) {
			if(e.getClass() ==  SocketException.class) {
				System.out.println("Server disconnected");
				isConnected = false;
			} else if(e.getClass() ==  ConnectException.class) {
				System.out.println("Cannot connect to server");
				isServerUp = false;
			} else if(e.getClass() ==  BindException.class) {
				System.out.println("Cannot bind to port " + port);
				JOptionPane.showMessageDialog(null,
						"Cannot bind to port " + port,
						"Bind error",
						JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			} else if(e.getClass() == NoRouteToHostException.class) {
				System.out.println("Internet connection problem");
				isServerUp = false;
			} else if(e.getClass() == EOFException.class) {
				System.out.println("Server disconnected");
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
	
	public String toString () {
		
		return "TCP Client";
	}

	@Override
	public void setConnected(boolean connected) {
		
		this.isConnected = true;
	}

}
