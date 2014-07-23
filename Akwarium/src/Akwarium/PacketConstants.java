package Akwarium;


public interface PacketConstants {
	
	public int UPDATE_COORDINATES = 0x1;
	public int ADD_ANIMAL = 0x2;
	public int REMOVE_ANIMAL = 0x3;
	public int INITIALIZE_IMAGES  = 0x4;
	public int CONNECTION_INITIALIZATION = 0x5;
	public int SHARK_UPDATE = 0x6;
	public int SETTINGS = 0x81;

	public static enum packet {
		
		UPDATE_COORDINATES(0x1, 11),
		ADD_ANIMAL(0x2, 14),
		REMOVE_ANIMAL(0x3, 2),
		INITIALIZE_IMAGES(0x4, 6),
		CONNECTION_INITIALIZATION(0x5, 4),
		SHARK_UPDATE(0x6, 12),
		SETTINGS(0x81, 8);
		
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
			
			return 0;
		}
	}
}
