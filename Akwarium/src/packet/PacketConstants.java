package packet;

import java.lang.reflect.Method;

import akwarium.Aquarium;

public interface PacketConstants {

	// Data
	public short HELLO_MESSAGE = (short) 0x8000;
	public short UPDATE_COORDINATES = (short) 0x8101;
	public short UPDATE_PLAYERS = (short) 0x8102;
	public short ADD_ANIMAL = (short) 0x8201;
	public short REMOVE_ANIMAL = (short) 0x8202;
	public short INITIALIZE_IMAGES  = (short) 0x8301;
	public short CONNECTION_INITIALIZATION = (short) 0x8302;
	public short IMAGES_INIT_END = (short) 0x8303;
	public short UPDATE_POINTS = (short) 0x8401;
	public short SETTINGS = (short) 0x85FF;
	
	// Responses
	public short OK = (short) 0x0000;
	public short FAIL = (short) 0x0001;
	public short CONNECTED = (short) 0x0002;
	public short DISCONNECTED = (short) 0x0003;
	public short ERROR = (short) 0xFFFF;
	public short INVALID_PACKET = (short) 0xFF00;
	public short ANIMAL_NOT_EXIST_ERROR = (short) 0xFF01;
	public short IMAGE_INDEX_OUT_OF_BOUNDS = (short) 0xFF02;
	
	public int MAX_PACKET_LENGTH = 32;
	
	
	public class PacketBlock {

		private byte[] buffer;
		private boolean isTcpPacket;
		private int length;

		PacketBlock(byte[] buffer, boolean isTcpPacket) {

			this.isTcpPacket = isTcpPacket;
			this.buffer = buffer;
			this.length = buffer.length;
		}

		public boolean isTcpPacket() {

			return isTcpPacket;
		}

		public byte[] getData() {

			return buffer;
		}

		public int size() {

			return length;
		}
	}

	
	public interface Trigger {
		
		void call(Object... args);
	}


	public enum Packet {

		
		// Data (length without header)
		UPDATE_COORDINATES( PacketConstants.UPDATE_COORDINATES, new Object[]{(short)0, (int)0, (int)0, (byte)0}, "updateCoordinates" ),
		ADD_ANIMAL( PacketConstants.ADD_ANIMAL, new Object[]{(short)0, (byte)0, (byte)0, (int)0, (int)0, (short)0}, "addAnimal" ),
		REMOVE_ANIMAL( PacketConstants.REMOVE_ANIMAL, new Object[]{(short)0}, "removeAnimal" ),
		INITIALIZE_IMAGES( PacketConstants.INITIALIZE_IMAGES, new Object[]{(byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (short)0}, "initializeImages" ),
		CONNECTION_INITIALIZATION( PacketConstants.CONNECTION_INITIALIZATION, new Object[]{(int)0}, "connectionInitialization" ),
		UPDATE_PLAYERS( PacketConstants.UPDATE_PLAYERS, new Object[]{(byte)0, (int)0, (int)0, (short)0, (byte)0}, "updatePlayers" ),
		UPDATE_POINTS( PacketConstants.UPDATE_POINTS, new Object[]{(byte)0, (int)0, (byte)0}, "updatePoints" ),
		SETTINGS( PacketConstants.SETTINGS, new Object[]{(int)0, (int)0}, "settings" ),
		IMAGES_INIT_END( PacketConstants.IMAGES_INIT_END, new Object[]{(boolean)false}, "imagesInitEnd" ),
		HELLO_MESSAGE( PacketConstants.HELLO_MESSAGE, new Object[]{(int)0}, "helloMessage" ),
		
		// Responses
		OK( PacketConstants.OK ), 
		FAIL( PacketConstants.FAIL ),
		ERROR( PacketConstants.ERROR ),
		INVALID_PACKET( PacketConstants.INVALID_PACKET ),
		ANIMAL_NOT_EXIST_ERROR( PacketConstants.ANIMAL_NOT_EXIST_ERROR ),
		IMAGE_INDEX_OUT_OF_BOUNDS( PacketConstants.IMAGE_INDEX_OUT_OF_BOUNDS ),
		CONNECTED( PacketConstants.CONNECTED ),
		DISCONNECTED( PacketConstants.DISCONNECTED );
		
		
		private short op;
		private Object[] seq; 
		private boolean response;
		private String triggerName;
		private Method trigger;
		public static final Packet[] DATA_PACKETS = {UPDATE_COORDINATES, ADD_ANIMAL, REMOVE_ANIMAL, INITIALIZE_IMAGES,
			CONNECTION_INITIALIZATION, UPDATE_PLAYERS, UPDATE_POINTS, SETTINGS, IMAGES_INIT_END, HELLO_MESSAGE};
		public static final Packet[] RESPONSE_PACKETS = {OK, FAIL, ERROR, INVALID_PACKET, ANIMAL_NOT_EXIST_ERROR, 
			IMAGE_INDEX_OUT_OF_BOUNDS, CONNECTED, DISCONNECTED};
		
		
		Packet (short op) {
			
			this.op = op;
			this.response = true;
		}

		Packet (short op, Object[] seq, String triggerName) {
			this.op = op;
			this.seq = seq;
			this.triggerName = triggerName;
			
		}
		
		public boolean isResponse () {
			
			return response;
		}

		public int op () {
			return op;
		}

		public Object[] seq () {
			return seq;
		}
		
		public static Packet getPacketByOP (short op) {
			
			for(Packet p : Packet.values()) {
				
				if(p.op == op)
					return p;
			}
			
			return null;
		}
		
		public void setTrigger (Object o) {
			
			try {
				trigger = o.getClass().getMethod(triggerName, Object[].class);
			} catch (NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		/*
		public static int getSize (int op) {
			

			if(op == UPDATE_COORDINATES.op())
				return UPDATE_COORDINATES.length();
			else if(op == ADD_ANIMAL.op())
				return ADD_ANIMAL.length();
			else if(op == REMOVE_ANIMAL.op())
				return REMOVE_ANIMAL.length();
			else if(op == INITIALIZE_IMAGES.op())
				return INITIALIZE_IMAGES.length();
			else if(op == CONNECTION_INITIALIZATION.op())
				return CONNECTION_INITIALIZATION.length();
			else if(op == SHARK_UPDATE.op())
				return SHARK_UPDATE.length();
			else if(op == SETTINGS.op())
				return SETTINGS.length();
			else if(op == UPDATE_POINTS.op())
				return UPDATE_POINTS.length();

			return 0;
		}
		*/
	}
}
