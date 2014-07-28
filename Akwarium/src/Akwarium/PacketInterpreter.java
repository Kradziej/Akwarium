package Akwarium;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public abstract class PacketInterpreter implements PacketConstants {
	
	private static Aquarium aq;
	private static int iv;
	
	public static int interpret(int op, InputStream packetInput) throws IOException {
		
		int index = 0;
		int code = 0;
		int x;
		int y;
		int v;
		int direction;
		byte[] buffer = new byte[64];
	
		switch(op) {
		
		case UPDATE_COORDINATES:
			
			packetInput.read(buffer, 0, packet.UPDATE_COORDINATES.length());
			index = (buffer[0] << 24) >>> 24 | ((buffer[1] << 24) >>> 16) & 0xFFFF;

			if(aq.getAnimal(index) == null)
				return 0;        // animals not ready
			
			x = (buffer[2] << 24) >>> 24 | ((buffer[3] << 24) >>> 16) | ((buffer[4] << 24) >>> 8) | (buffer[5] << 24);
			y = (buffer[6] << 24) >>> 24 | ((buffer[7] << 24) >>> 16) | ((buffer[8] << 24) >>> 8) | (buffer[9] << 24);
			direction = buffer[10] & 0xFF;
			
			aq.updateCooridates(index, x, y, direction);
			return 0;
		
		case ADD_ANIMAL:
			
			packetInput.read(buffer, 0, packet.ADD_ANIMAL.length());
			index =  (buffer[0] << 24) >>> 24 | ((buffer[1] << 24) >>> 16);
			code = buffer[2] & 0xFF;
			int imageIndex = buffer[3] & 0xFF;
			x = (buffer[4] << 24) >>> 24 | ((buffer[5] << 24) >>> 16) | ((buffer[6] << 24) >>> 8) | (buffer[7] << 24);
			y = (buffer[8] << 24) >>> 24 | ((buffer[9] << 24) >>> 16) | ((buffer[10] << 24) >>> 8) | (buffer[11] << 24);
			v = ((buffer[12] << 24) >>> 24 | ((buffer[13] << 24) >>> 16)) & 0xFFFF;
			
			
			try {
				aq.addAnimal(code, imageIndex, index, x, y, v);
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
			index = (buffer[0] << 24) >>> 24 | ((buffer[1] << 24) >>> 16) & 0xFFFF;
			aq.removeAnimal(index);
			return 0;
			
		case INITIALIZE_IMAGES:
			
			packetInput.read(buffer, 0, packet.INITIALIZE_IMAGES.length()+1);
			code = buffer[0] & 0xFF;
			index = buffer[1] & 0xFF;
			Color color = new Color(buffer[2] & 0xFF, buffer[3] & 0xFF, buffer[4] & 0xFF);
			int width = buffer[5] & 0xFF;
			if(Animal.initAnimalsClient(code, index, color, width))
				return 2;
			
			return 0;
			
		case CONNECTION_INITIALIZATION:
			
			packetInput.read(buffer, 0, packet.CONNECTION_INITIALIZATION.length());
			iv = (buffer[0] << 24) >>> 24 | ((buffer[1] << 24) >>> 16) | ((buffer[2] << 24) >>> 8) | (buffer[3] << 24);
			return 1;
			
		case SHARK_UPDATE:
			
			packetInput.read(buffer, 0, packet.SHARK_UPDATE.length());
			index = buffer[0] & 0xFF;
			
			if(index == 0 && aq.getOwner() == null)
				return 0;
			
			if(index == 1 && aq.getPlayer() == null)
				return 0;

			x = (buffer[1] << 24) >>> 24 | ((buffer[2] << 24) >>> 16) | ((buffer[3] << 24) >>> 8) | (buffer[4] << 24);
			y = (buffer[5] << 24) >>> 24 | ((buffer[6] << 24) >>> 16) | ((buffer[7] << 24) >>> 8) | (buffer[8] << 24);
			v = ((buffer[9] << 24) >>> 24 | ((buffer[10] << 24) >>> 16)) & 0xFFFF;
			direction = buffer[11] & 0xFF;
			
			if(index == 1) {  //player coordinates incoming
				x = Math.round(x * (1/DrawAq.xScale()));
				y = Math.round(y * (1/DrawAq.yScale()));
			}
			aq.updateSharks(index, x, y, v, direction);
			return 0;
				
			
		case SETTINGS:
			
			packetInput.read(buffer, 0, packet.SETTINGS.length());
			int w = (buffer[0] << 24) >>> 24 | ((buffer[1] << 24) >>> 16) | ((buffer[2] << 24) >>> 8) | (buffer[3] << 24);
			int h = (buffer[4] << 24) >>> 24 | ((buffer[5] << 24) >>> 16) | ((buffer[6] << 24) >>> 8) | (buffer[7] << 24);
			// calculate and set scale
			Dimension d = DrawAq.getResolution();
			DrawAq.setScales((float)(w / d.getWidth()), (float)(h / d.getHeight()));
			return 0;
		
		
		case UPDATE_POINTS:
			
			packetInput.read(buffer, 0, packet.UPDATE_POINTS.length());
			index = buffer[0] & 0xFF;
			int points = (buffer[1] << 24) >>> 24 | ((buffer[2] << 24) >>> 16) | ((buffer[3] << 24) >>> 8) | (buffer[4] << 24);
			int health =  buffer[5] & 0xFF;
			aq.updatePoints(index, points, health);
		}
		
		return -1;
	}


	public static void setAq(Aquarium aquarium) {
		
		aq = aquarium;
	}
	
	
}
