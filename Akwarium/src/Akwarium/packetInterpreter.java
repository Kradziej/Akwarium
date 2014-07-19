package Akwarium;

import java.awt.Color;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public abstract class packetInterpreter implements packetConstants {
	
	private static Aquarium Aq;
	private static int iv;

	public static int interpret(int op, InputStream packetInput) throws IOException {
		
		int index = 0;
		int code = 0;
		byte[] buffer = new byte[256];
	
		switch(op) {
		
		case UPDATE_COORDINATES:
			
			packetInput.read(buffer, 0, packet.UPDATE_COORDINATES.length());
			index = (buffer[0] << 24) >>> 24 | ((buffer[1] << 24) >>> 16);

			if(Aq.getAnimal(index) == null)
				return 0;        // animals not ready
			
			int x = (buffer[2] << 24) >>> 24 | ((buffer[3] << 24) >>> 16) | ((buffer[4] << 24) >>> 8) | (buffer[5] << 24);
			int y = (buffer[6] << 24) >>> 24 | ((buffer[7] << 24) >>> 16) | ((buffer[8] << 24) >>> 8) | (buffer[9] << 24);
			int v1 = (buffer[10] << 24) >>> 24 | ((buffer[11] << 24) >>> 16) | ((buffer[12] << 24) >>> 8) | (buffer[13] << 24);
			int v2 = (buffer[14] << 24) >>> 24 | ((buffer[15] << 24) >>> 16) | ((buffer[16] << 24) >>> 8) | (buffer[17] << 24);
			
			float[] vector = {(float)v1/FLOAT_PRECISION, (float)v2/FLOAT_PRECISION};
			Aq.updateCooridates(index, x, y, vector);
			return 0;
		
		case ADD_ANIMAL:
			
			packetInput.read(buffer, 0, packet.ADD_ANIMAL.length());
			index =  (buffer[0] << 24) >>> 24 | ((buffer[1] << 24) >>> 16);
			code = (int)buffer[2] & 0xFF;
			int imageIndex = (int)buffer[3] & 0xFF;
			x = (buffer[4] << 24) >>> 24 | ((buffer[5] << 24) >>> 16) | ((buffer[6] << 24) >>> 8);
			y = (buffer[7] << 24) >>> 24 | ((buffer[8] << 24) >>> 16) | ((buffer[9] << 24) >>> 8);
			int v = (buffer[10] << 24) >>> 24 | ((buffer[11] << 24) >>> 16);
			System.out.println(index);
			
			try {
				Aq.addAnimal(code, imageIndex, index, x, y, v);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				System.out.println("Cannot create pet");
				e.printStackTrace();
				System.exit(-1);
			}
			return 0;
			
		case REMOVE_ANIMAL:
			
			packetInput.read(buffer, 0, packet.REMOVE_ANIMAL.length());
			index = (buffer[0] << 24) >>> 24 | ((buffer[1] << 24) >>> 16);
			Aq.removeAnimal(index);
			return 0;
			
		case INITIALIZE_IMAGES:
			
			packetInput.read(buffer, 0, packet.INITIALIZE_IMAGES.length()+1);
			code = (int)buffer[0] & 0xFF;
			index = (int)buffer[1] & 0xFF;
			Color color = new Color(buffer[2] & 0xFF, buffer[3] & 0xFF, buffer[4] & 0xFF);
			int width = (int)buffer[5] & 0xFF;
			Animal.initAnimalsClient(code, index, color, width);
			return 0;
			
		case CONNECTION_INITIALIZATION:
			
			packetInput.read(buffer, 0, packet.CONNECTION_INITIALIZATION.length());
			iv = (buffer[0] << 24) >>> 24 | ((buffer[1] << 24) >>> 16) | ((buffer[2] << 24) >>> 8) | (buffer[3] << 24);
			return 1;
		
		}
		
		return -1;
	}


	public static void setAq(Aquarium aquarium) {
		
		Aq = aquarium;
	}
	
	
}
