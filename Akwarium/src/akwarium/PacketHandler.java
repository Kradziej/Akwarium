package akwarium;

import java.io.DataInputStream;
import java.io.IOException;

public abstract class PacketHandler {

	PacketInterpreter packetInterpreter;
	ResponseHandler responseHandler;
	
	public PacketHandler() {}
	
	public PacketHandler(PacketInterpreter packetInterpreter, ResponseHandler responseHandler) {
		
		this.packetInterpreter = packetInterpreter;
		this.responseHandler = responseHandler;
	}
	
	
	public boolean interpret(short op, DataInputStream packetInput) throws IOException {
		
		return packetInterpreter.interpret(op, packetInput) | responseHandler.interpret(op, packetInput);
	}
	
	
	
}
