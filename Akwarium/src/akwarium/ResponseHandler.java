package akwarium;

import java.io.DataInputStream;
import java.io.IOException;

public class ResponseHandler extends PacketHandler implements PacketConstants {
	
	private int retries;
	private static final int MAX_RETRIES = 5;
	
	public boolean interpret(short op, DataInputStream packetInput) throws IOException {
		
		switch(op) {
		
		case OK:
			
			// positive answer
			break;
			
		case FAIL:
			
			// negative answer
			break;
			
		case ERROR:
			
			System.out.println("Transmission error");
			return false;
			
		case INVALID_PACKET:
			
			System.out.println("Invalid packet");
			retries++;
			break;
			
		case ANIMAL_NOT_EXIST_ERROR:
			
			// ??????????
			break;
			
		case IMAGE_INDEX_OUT_OF_BOUNDS:
			
			// something wrong with image init
			System.out.println("Image Initialization error: image index out of bounds");
			return false;
			
		}
		
		// if false --> INVALID_RESPONSE 
		if (retries == MAX_RETRIES) 
			return false;
		
		return true;   
		
	}
}
