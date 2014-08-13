package packet;

import java.io.DataInputStream;
import java.io.IOException;

import packet.PacketConstants.Packet;

public abstract class PacketHandler {


	public static boolean interpret (short op, DataInputStream packetInput)  {
		
		Packet p = Packet.getPacketByOP(op);
		if(p == null) {
			System.out.println(String.format("Invalid packet header 0x%X", op));
			PacketSender.getSender().sendResponse(Packet.INVALID_PACKET);
			return false;
		}
		
		Object[] val = p.seq().clone();
		int index = 0;	
		
		
		try {
		for(Object o : p.seq()) {
			if(o.getClass() == Integer.class)
				val[index] = packetInput.readInt();
			else if(o.getClass() == Short.class)
				val[index] = packetInput.readShort();
			else if(o.getClass() == Byte.class)
				val[index] = packetInput.readByte();
			else if(o.getClass() == Double.class)
				val[index] = packetInput.readDouble();
			else if(o.getClass() == Float.class)
				val[index] = packetInput.readFloat();
			else if(o.getClass() == Boolean.class)
				val[index] = Boolean.valueOf(packetInput.readBoolean());
			
			index++;
		}
		} catch (IOException e) {
			System.out.println("Cannot read data from socket: " + e.getClass().toString());
			return false;
		}
		
		
		p.invoke(val);
		return true;
	}
	
}
