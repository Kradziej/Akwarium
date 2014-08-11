package packet;

import java.awt.Color;
import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import akwarium.Aquarium;
import akwarium.DrawAq;
import static packet.PacketConstants.*;

class PacketInterpreter__ {

	private static Aquarium aq;
	private static int iv;
	

	
	public int interpret(short op, DataInputStream packetInput) throws IOException {

		short index = 0;
		int code = 0;
		int x = 0;
		int y = 0;
		int v = 0;
		int direction = 0;
		
		switch (op) {

		case UPDATE_COORDINATES:

			// short | int | int | byte
			index = packetInput.readShort();

			if (aq.getAnimal(index) == null)   // NEEDED?
				return 1; // animals not ready

			x = packetInput.readInt();
			y = packetInput.readInt();
			direction = packetInput.readByte();

			aq.updateCooridates(index, x, y, direction);
			return 1;

		case ADD_ANIMAL:

			// short | byte | byte | int | int | short
			index = packetInput.readShort();
			code = packetInput.readByte();
			int imageIndex = packetInput.readByte();
			x = packetInput.readInt();
			y = packetInput.readInt();
			v = packetInput.readShort();

			try {
				aq.addAnimal(code, imageIndex, index, x, y, v);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				System.out.println("Cannot create pet");
				e.printStackTrace();
				System.exit(-1);
			}
			return 1;

		case REMOVE_ANIMAL:

			// short
			index = packetInput.readShort();
			aq.removeAnimal(index);
			return 1;

		case INITIALIZE_IMAGES:

			// byte | byte | colorRGB(byte | byte | byte) | short
			code = packetInput.readByte();
			index = packetInput.readByte();
			Color color = new Color(packetInput.readByte(), packetInput.readByte(), packetInput.readByte());
			int width = packetInput.readByte();
			
			return 1;

		case CONNECTION_INITIALIZATION:

			// int
			iv = packetInput.readInt();
			return 1;

		case UPDATE_PLAYERS:

			// byte | int | int | short | byte 
			index = packetInput.readByte();
			
			if (index == 0 && aq.getOwner() == null)   // I DONT KNOW ABOUT THIS
				return 1;

			if (index == 1 && aq.getPlayer() == null)
				return 1;

			x = packetInput.readInt();
			y = packetInput.readInt();
			v = packetInput.readShort();
			direction = packetInput.readByte();
			
			if (index == 1) { // player coordinates incomingC   // LOL NOPEEEEEEEEEEEEEEEEEEE
				x = Math.round(x * (1 / DrawAq.xScale()));
				y = Math.round(y * (1 / DrawAq.yScale()));
			}
			aq.updateSharks(index, x, y, v, direction);
			return 1;

		case SETTINGS:

			// int | int
			int w = packetInput.readInt();
			int h = packetInput.readInt();
			
			// calculate and set scale
			Dimension d = DrawAq.getResolution();
			DrawAq.setScales((float) (w / d.getWidth()),
					(float) (h / d.getHeight()));
			return 1;

		case UPDATE_POINTS:

			// byte | int | byte
			index = packetInput.readByte();
			int points = packetInput.readInt();
			int health = packetInput.readByte();

			aq.updatePoints(index, points, health);
			return 1;
		}

		return 0;
	}
	

	public static void setAq(Aquarium aquarium) {

		aq = aquarium;
	}

}
