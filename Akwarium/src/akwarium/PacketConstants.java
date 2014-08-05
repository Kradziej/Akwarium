package akwarium;


public interface PacketConstants {

	public short UPDATE_COORDINATES = (short) 0x8101;
	public short UPDATE_PLAYERS = (short) 0x8102;
	public short ADD_ANIMAL = (short) 0x8201;
	public short REMOVE_ANIMAL = (short) 0x8202;
	public short INITIALIZE_IMAGES  = (short) 0x8301;
	public short CONNECTION_INITIALIZATION = (short) 0x8302;
	public short IMAGES_INIT_END = (short) 0x8303;
	public short UPDATE_POINTS = (short) 0x8401;
	public short SETTINGS = (short) 0x85FF;


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

		public byte[] getBuffer() {

			return buffer;
		}

		public int size() {

			return length;
		}
	}



	public static enum packet {

		UPDATE_COORDINATES(PacketConstants.UPDATE_COORDINATES, 11),
		ADD_ANIMAL(PacketConstants.ADD_ANIMAL, 14),
		REMOVE_ANIMAL(PacketConstants.REMOVE_ANIMAL, 2),
		INITIALIZE_IMAGES(PacketConstants.INITIALIZE_IMAGES, 6),
		CONNECTION_INITIALIZATION(PacketConstants.CONNECTION_INITIALIZATION, 4),
		UPDATE_PLAYERS(PacketConstants.UPDATE_PLAYERS, 12),
		UPDATE_POINTS(PacketConstants.UPDATE_POINTS, 6),
		SETTINGS(PacketConstants.SETTINGS, 8),
		IMAGES_INIT_END(PacketConstants.IMAGES_INIT_END, 3);

		private final int op;
		private final int len;

		packet (int op, int len) {
			this.op = op;
			this.len = len;
		}

		public int op () {
			return op;
		}

		public int length () {
			return len;
		}

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
	}
}
