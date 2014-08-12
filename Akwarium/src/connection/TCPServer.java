package connection;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JOptionPane;

import packet.PacketConstants;
import packet.ResponseHandler;
import packet.PacketConstants.Packet;

public class TCPServer extends Connection implements Runnable, PacketConstants {

	private byte[] bufferIn = new byte[64];
	private byte[] bufferOut = new byte[64];
	private UDPServer sendCoor;
	private boolean isConnected;
	private boolean disconnected;
	private boolean isSettingsReady;
	private boolean isGraphicsReady;
	private boolean runServer = true;
	private InetAddress IPAddress;     // server client is connected with
	private PipedInputStream tcpInput;
	private PipedInputStream udpInput;
	private InputStream clientIn;
	private OutputStream clientOut;
	private ResponseHandler rHandler;
	Thread t;


	TCPServer () {

		port = 4945;
		rHandler = new ResponseHandler();
		
	}

	TCPServer (PipedInputStream tcp, PipedInputStream udp) {

		this();
		tcpInput = tcp;
		udpInput = udp;
	}

	@Override
	public void run () {


		ServerSocket server = null;
		Socket client = null;
		InputStream in = null;
		OutputStream out = null;
		int bytesToRead;
		int retries = 0;


		try {

			server = new ServerSocket(port);
			server.setSoTimeout(0);
			client = server.accept();
			IPAddress = client.getInetAddress();
			clientIn = client.getInputStream();
			clientOut = client.getOutputStream();
			DataInputStream dataClientIn = new DataInputStream(clientIn);
			short op;
			
			while (runServer) {
				
				op = dataClientIn.readShort();
				runServer = rHandler.interpret(op, dataClientIn);
			}
			
			while ((op = tcpInput.read()) != -1) {
				bytesToRead = Packet.getSize(op);
				bufferOut[0] = (byte)op;
				tcpInput.read(bufferOut, 1, bytesToRead);
				out.write(bufferOut, 0, bytesToRead+1);
				in.read(bufferIn);

				switch (bufferIn[0] & 0x000000FF) {

				case 0x00:     // ok
					retries = 0;
					break;

				case 0xFF:    // failed
					retries++;
					if(retries > 4) {
						System.out.println("DESYNCHRONIZATION");
						isConnected = false;
						client.close();
						server.close();
						return;
					}
					break;

				case 0xFE:   // iv ready
					sendCoor = new UDPServer(client.getInetAddress(), port);
					sendCoor.startThread();
					isConnected = true;
					break;

				case 0xFD:   // graphics ready
					isGraphicsReady = true;
					break;
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
	

	public OutputStream getSocketOutput() {
		
		return clientOut;
	}

	@Override
	public void startThread () {

		t = new Thread(this);
		t.start();
	}
	
	public void terminate () {
		
		runServer = false;
	}

	@Override
	public boolean isConnected () {

		return isConnected;
	}

	@Override
	public boolean isDisconnected () {

		return disconnected;
	}

	@Override
	public boolean isGraphicsReady () {

		return isGraphicsReady;
	}

	@Override
	public InetAddress getIPAddress () {

		return IPAddress;
	}

	@Override
	public int getNextPort () {

		port++;
		return port;
	}

}
