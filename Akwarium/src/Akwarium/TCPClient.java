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
import java.util.Random;

public class TCPClient extends PacketInterpreter implements Runnable {
	
	private byte[] buffer = new byte[64];
	private static final int PORT = 4945;
	private int iv;
	private boolean isConnected;
	private InetAddress IPAddress;
	private UDPClient recCoor;
	private boolean runServer = true;
	private Thread t;
	
	
	TCPClient (InetAddress IPAddress) {
		
		this.IPAddress = IPAddress;
	}
	
	public void run () {
		
		
		// nowe 2 klasy server i klient posiadaja Aq zeby odswierzac liste i synchro daj
		// Animal musi miec handle do servera i tworzyc nowe polaczenie wysylajace gdy polozenie jest odswiezane lub inne pierdy
		// wykorzystujac indeks w tablicy czyli kazdy nowy obiekt Animal musi miec tez zapisany w sobie index (DODAJ!)
		// grajacy jest klientem i nasluchuje zmian
		// serwer wysywa zmiany tylko gdy je dostanie od threadow animalsow by index
		// serwer moze wysylac rozne komendy take usun dodaj tylko ze wtedy nie robimy threada dla tworzonego animalsa
		// 1 bajt odswiezanie pozycji0x0/dodawanie0x01/usuwanie0x02/ komenda
		// odswiezanie pozycji 0x1  --> UDP
		// 2 bajty indeks zwierzaka na liscie
		// 3 bajty pozycja x
		// 3 bajty pozycja y
		// 6 bajtow vektorki
		// dodawanie 0x2
		// 2 bajty indeks zwierzaka na liscie
		// 1 bajt typ zwierzaka 0x0 ryba / 0x1 zolw / 0x2 meduza
		// 1 bajt index obrazka dla tej ryby (255)
		// 3 bajty pozycja x poczatkowa
		// 3 bajty pozycja y poczatkowa
		// 2 bajt predkosc
		// x bajtow nazwa
		// usuwanie 0x03
		// 2 bajty indeks zwierzaka na liscie
		// inicjalizacja 0x04 -> dane do inicjalizacji po czym 0x04 zeby wywolac wyswietlanie
		// dodawane po kolei
		// 1 bajt typ zwierzaka 0x0 ryba / 0x1 zolw / 0x2 meduza
		// 1 bajt indeks na liscie obrazkow
		// 3 bajty color R / G / B
		// 1 bajt width
		// wyslanie iv 0x05
		
		
		

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
			client.connect(new InetSocketAddress(IPAddress, PORT), 0); // socket.accept()
			out = client.getOutputStream();
			out.write(buffer, 0, 5);   // send hello message
			in = client.getInputStream();
			int op;
			while ((op = in.read()) != -1) {
				
				switch(interpret(op, in)) {
				case 1:
					buffer[0] = (byte)0xFE;   // iv resolved
					buffer[1] = (byte)0x00;
					out.write(buffer, 0, 2);
					recCoor = new UDPClient(iv, PORT+1);
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
			if(e.getClass() ==  ConnectException.class)
				System.out.println("Cannot connect to server");
			if(e.getClass() ==  BindException.class)
				System.out.println("Cannot bind to port " + PORT);
			if(e.getClass() == NoRouteToHostException.class)
				System.out.println("Client disconnected");
			else
				System.out.println("Cannot read/send data to server");
				
			e.printStackTrace();
			System.exit(-1);
		}
		
		
		try {
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Cannot close socket");
			System.exit(-1);
		}
		
		recCoor.terminate();
	}
	
	
	public void startThread () {
		
		t = new Thread(this);
		t.start();
	}
	
	public boolean isConnected () {
		
		return isConnected;
	}

}
