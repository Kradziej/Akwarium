package packet;

import java.io.DataInputStream;
import java.io.IOException;

class ResponseHandler implements PacketConstants {
	
	private int retries;
	private static final int MAX_RETRIES = 5;
	
	public int interpret(short op, DataInputStream packetInput) throws IOException {
		
		switch(op) {
		
		case OK:
			
			// positive answer
			return 1;
			
		case FAIL:
			
			// negative answer
			return 1;
			
		case ERROR:
			
			System.out.println("Transmission error");
			return -1;
			
		case INVALID_PACKET:
			
			System.out.println("Invalid packet sent");
			retries++;
			return 1;
			
		case ANIMAL_NOT_EXIST_ERROR:
			
			// ??????????
			return 1;
			
		case IMAGE_INDEX_OUT_OF_BOUNDS:
			
			// something wrong with image init
			System.out.println("Image Initialization error: image index out of bounds");
			return 1;
			
		}
		
		// if false --> INVALID_RESPONSE 
		if (retries == MAX_RETRIES) {
			System.out.println("DESYNCHRONIZATION");
			return -1;
		}
			
		
		return 0;   
		
	}
}
