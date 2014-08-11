package packet;

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
	
	
	public class packetBlock {

		private byte[] buffer;
		private boolean isTcpPacket;
		private int length;

		packetBlock(byte[] buffer, boolean isTcpPacket) {

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

	
	public interface Trigger<T> extends PacketInterpreter {
		
		void call(Object... args);
	}


	public enum packet {

		
		// Data (length without header)
		UPDATE_COORDINATES( PacketConstants.UPDATE_COORDINATES, new Object[]{(short)0, (int)0, (int)0, (byte)0}, new Trigger<Aquarium>() {
			Aquarium ref;
			public void call(Object... args) { ref.updateCoordinates(args); }
		}),
		ADD_ANIMAL( PacketConstants.ADD_ANIMAL, new Object[]{(short)0, (byte)0, (byte)0, (int)0, (int)0, (short)0} ),
		REMOVE_ANIMAL( PacketConstants.REMOVE_ANIMAL, new Object[]{(short)0} ),
		INITIALIZE_IMAGES( PacketConstants.INITIALIZE_IMAGES, new Object[]{(byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (short)0} ),
		CONNECTION_INITIALIZATION( PacketConstants.CONNECTION_INITIALIZATION, new Object[]{(int)0} ),
		UPDATE_PLAYERS( PacketConstants.UPDATE_PLAYERS, new Object[]{(byte)0, (int)0, (int)0, (short)0, (byte)0} ),
		UPDATE_POINTS( PacketConstants.UPDATE_POINTS, new Object[]{(byte)0, (int)0, (byte)0} ),
		SETTINGS( PacketConstants.SETTINGS, new Object[]{(int)0, (int)0} ),
		IMAGES_INIT_END( PacketConstants.IMAGES_INIT_END, new Object[]{(boolean)false} ),
		HELLO_MESSAGE( PacketConstants.HELLO_MESSAGE, new Object[]{(int)0} ),
		
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
		
		packet (short op) {
			
			this.op = op;
			this.response = true;
		}

		packet (short op, Object[] seq) {
			this.op = op;
			this.seq = seq;
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
		
		public static packet getPacketByOP (short op) {
			
			for(packet p : packet.values()) {
				
				if(p.op == op)
					return p;
			}
			
			return null;
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
