package packet;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import packet.PacketConstants.packet;

public abstract class PacketHandler {

	PacketInterpreter packetInterpreter;
	ResponseHandler responseHandler;
	
	public PacketHandler () {
		
		this.packetInterpreter = new PacketInterpreter();
		this.responseHandler = new ResponseHandler();
	}
	
	
	public boolean interpret0000 (short op, DataInputStream packetInput)  {
		
		ArrayList<Object> val = new ArrayList<>();
		 
		packet p = packet.getPacketByOP(op);
		for(int i : p.seq()) {
			if(i == 4)
				packetInput.readInt()
		}
	}
	
	
	
	
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
	}
	
}
