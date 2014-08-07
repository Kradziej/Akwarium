package akwarium;

import java.io.DataInputStream;
import java.io.IOException;

import akwarium.PacketConstants.packet;

public abstract class PacketHandler {

	PacketInterpreter packetInterpreter;
	ResponseHandler responseHandler;
	
	public PacketHandler () {
		
		this.packetInterpreter = new PacketInterpreter();
		this.responseHandler = new ResponseHandler();
	}
	
	 
	public boolean interpret (short op, DataInputStream packetInput) throws IOException {
		
		int count = 1;
		int result = packetInterpreter.interpret(op, packetInput);
		int nextResult = responseHandler.interpret(op, packetInput);
		result |= (nextResult != -1) ? nextResult << count : -1;
		count++;
		
		if(result == 0) {
			System.out.println("Invalid packet received");
			PacketSender.getSender().sendResponse(packet.INVALID_PACKET);
		}
		
		return result >= 0 ? true : false;
	}
	
	
	
}
