package packet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

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
		OK( PacketConstants.OK, "ok" ), 
		FAIL( PacketConstants.FAIL, "fail" ),
		ERROR( PacketConstants.ERROR, "error" ),
		INVALID_PACKET( PacketConstants.INVALID_PACKET, "invalidPacket" ),
		ANIMAL_NOT_EXIST_ERROR( PacketConstants.ANIMAL_NOT_EXIST_ERROR, "animalNotExistError" ),
		IMAGE_INDEX_OUT_OF_BOUNDS( PacketConstants.IMAGE_INDEX_OUT_OF_BOUNDS, "imageIndexOutOfBounds" ),
		CONNECTED( PacketConstants.CONNECTED, "connected" ),
		DISCONNECTED( PacketConstants.DISCONNECTED, "disconnected" );
		
		
		private short op;
		private Object[] seq; 
		private boolean response;
		private String triggerName;
		private Method trigger;
		private Object triggerObj;
		private static final HashMap<Short, Packet> opToEnum = new HashMap<>(); 
		public static final Packet[] DATA_PACKETS = {UPDATE_COORDINATES, ADD_ANIMAL, REMOVE_ANIMAL, INITIALIZE_IMAGES,
			CONNECTION_INITIALIZATION, UPDATE_PLAYERS, UPDATE_POINTS, SETTINGS, IMAGES_INIT_END, HELLO_MESSAGE};
		public static final Packet[] RESPONSE_PACKETS = {OK, FAIL, ERROR, INVALID_PACKET, ANIMAL_NOT_EXIST_ERROR, 
			IMAGE_INDEX_OUT_OF_BOUNDS, CONNECTED, DISCONNECTED};
		
		
		static {
			
			for(Packet p : Packet.values()) {
				opToEnum.put(p.op, p);
			}
		}
		

		Packet (short op, String triggerName) {
			
			this.op = op;
			this.triggerName = triggerName;
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
			return opToEnum.get(op);
		}
		
		/**
		 * Set method which can be called by {@link #invoke()} method.   
		 * @param o
		 */
		public void setTrigger (Object o) {
			
			try {
				trigger = o.getClass().getMethod(triggerName, Object[].class);
				triggerObj = o;
			} catch (NoSuchMethodException | SecurityException e) {
				System.out.println("Cannot set trigger for " + this.toString() + ": " + e.getClass().toString());
				return;
			}
		}
		
		
		public static void setDataPacketsTrigger (Object o) {
			
			for(Packet p : DATA_PACKETS) {
				
				try {
					p.trigger = o.getClass().getMethod(p.triggerName, Object[].class);
					p.triggerObj = o;
				} catch (NoSuchMethodException | SecurityException e) {
					System.out.println("Cannot set trigger for " + p.toString() + ": " + e.getClass().toString());
					return;
				}
			}
			
		}
		
		public static void setResponsePacketsTrigger (Object o) {
			
			for(Packet p : RESPONSE_PACKETS) {
				
				try {
					p.trigger = o.getClass().getMethod(p.triggerName, Object[].class);
					p.triggerObj = o;
				} catch (NoSuchMethodException | SecurityException e) {
					System.out.println("Cannot set trigger for " + p.toString() + ": " + e.getClass().toString());
					return;
				}
			}
			
		}
		
		public void invoke(Object... args) {
			
			try {
				trigger.invoke(triggerObj, args);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				System.out.println("Cannot call method " + trigger.getName() + " from " + triggerObj.toString() + ": "
			+ e.getClass().toString());
			}
		}
		
		/**
		 * Check if trigger method for all enum constants was set.
		 * @return true if all enum constants have their own triggers.
		 */
		public static boolean checkTriggers () {
			
			for(Packet p : Packet.values()) {
				
				if(p.trigger == null)
					return false;
			}
			
			return true;
		}
		

	}
}
