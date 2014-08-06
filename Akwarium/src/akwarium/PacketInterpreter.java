package akwarium;

import java.awt.Color;
import java.awt.Dimension;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public class PacketInterpreter extends PacketHandler implements PacketConstants {

	private static Aquarium aq;
	private static int iv;
	
	
	public boolean interpret(short op, DataInputStream packetInput) throws IOException {

		short index = 0;
		int code = 0;
		int x = 0;
		int y = 0;
		int v = 0;
		int direction = 0;

		switch (op) {

		case UPDATE_COORDINATES:

			index = packetInput.readShort();

			if (aq.getAnimal(index) == null)
				return 0; // animals not ready

			x = packetInput.readInt();
			y = packetInput.readInt();
			direction = packetInput.readByte();

			aq.updateCooridates(index, x, y, direction);
			return 0;

		case ADD_ANIMAL:

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
			return 0;

		case REMOVE_ANIMAL:

			index = packetInput.readShort();
			aq.removeAnimal(index);
			return 0;

		case INITIALIZE_IMAGES:

			code = packetInput.readByte();
			index = packetInput.readByte();
			Color color = new Color(packetInput.readByte(), packetInput.readByte(), packetInput.readByte());
			int width = packetInput.readByte();
			
			if (Animal.initAnimalsClient(code, index, color, width))
				return 2;

			return 0;

		case CONNECTION_INITIALIZATION:

			iv = packetInput.readInt();
			return 1;

		case UPDATE_PLAYERS:

			index = packetInput.readByte();
			
			if (index == 0 && aq.getOwner() == null)
				return 0;

			if (index == 1 && aq.getPlayer() == null)
				return 0;

			x = packetInput.readInt();
			y = packetInput.readInt();
			v = packetInput.readShort();
			direction = packetInput.readByte();
			
			if (index == 1) { // player coordinates incoming
				x = Math.round(x * (1 / DrawAq.xScale()));
				y = Math.round(y * (1 / DrawAq.yScale()));
			}
			aq.updateSharks(index, x, y, v, direction);
			return 0;

		case SETTINGS:

			int w = packetInput.readInt();
			int h = packetInput.readInt();
			
			// calculate and set scale
			Dimension d = DrawAq.getResolution();
			DrawAq.setScales((float) (w / d.getWidth()),
					(float) (h / d.getHeight()));
			return 0;

		case UPDATE_POINTS:

			index = packetInput.readByte();
			int points = packetInput.readInt();
			int health = packetInput.readByte();

			aq.updatePoints(index, points, health);
		}

		return -1;
	}

	public static void setAq(Aquarium aquarium) {

		aq = aquarium;
	}

}
