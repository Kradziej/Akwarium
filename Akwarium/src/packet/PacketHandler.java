package packet;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import connection.Connection;
import packet.PacketConstants.Packet;

public class PacketHandler {

	private Connection con;
	
	public PacketHandler (Connection con) {
		
		this.con = con;
	}
	

	public boolean interpretResponse (PacketConstants.Packet p) {
		
		if(p.equals(Packet.INVALID_PACKET)) {
			System.out.println("Invalid packet send!");
			return false;
		} else if(p.equals(Packet.ERROR)) {
			System.out.println("Error packet received, disconnecting...");
			return false;
		}
		/*else if(p.equals(packet.CONNECTED)) {       ELSEWHERE THIS SHIT!!!
			con.setConnected(true);
		} else if(p.equals(packet.DISCONNECTED)) {
			con.setConnected(false);
		}*/
		
		return true;
	}
	
	public boolean interpret (short op, DataInputStream packetInput)  {
		
		ArrayList<Object> val = new ArrayList<>();
		Packet p = Packet.getPacketByOP(op);
		
		if(p == null) {
			System.out.println("Invalid packet header");
			PacketSender.getSender().sendResponse(Packet.INVALID_PACKET);
			return false;
		}
		
		if(p.isResponse())
			return interpretResponse(p);
		
		try {
		for(Object o : p.seq()) {
			if(o.getClass() == Integer.class)
				val.add(packetInput.readInt());
			else if(o.getClass() == Short.class)
				val.add(packetInput.readShort());
			else if(o.getClass() == Byte.class)
				val.add(packetInput.readByte());
			else if(o.getClass() == Double.class)
				val.add(packetInput.readDouble());
			else if(o.getClass() == Float.class)
				val.add(packetInput.readFloat());
			else if(o.getClass() == Boolean.class)
				val.add(Boolean.valueOf(packetInput.readBoolean()));
		}
		} catch (IOException e) {
			System.out.println("Cannot read data from ");
			e.printStackTrace();
			return false;
		}
		
		
		return true;
	}
	
	
	
	/*
	// returns true if packet is handled or false if packet indicates error
	public boolean interpret (short op, DataInputStream packetInput)  {
		
		// 1 -> packet interpreted / 0 -> packet not interpreted
		// 0 bit -> packetInterpreter
		// 1 bit -> responseHandler
		// mask equal to -1 indicates error
		int count = 1;
		int result = 0;
		int responseResult = 0;
		
		try {
			result = packetInterpreter.interpret(op, packetInput);
		} catch (IOException e) {
			System.out.println("Cannot read packet data: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		try {
			responseResult = responseHandler.interpret(op, packetInput);
		} catch (IOException e) {
			System.out.println("Cannot read packet data: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		result |= (responseResult != -1) ? responseResult << count : -1;
		count++;
		
		
		if(result == 0) {
			System.out.println("Invalid packet received");
			PacketSender.getSender().sendResponse(packet.INVALID_PACKET);
		}
		
		return result >= 0 ? true : false;
	}*/
	
}
