package Akwarium;

import java.awt.Color;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.Random;

public abstract class packetSender implements packetConstants {

	private static PipedOutputStream tcpOutput;
	private static PipedOutputStream udpOutput;
	private static int iv;
	
	public static int addAnimal (int code, int imageIndex, int index, int x, int y, int v) {
		
		 byte[] buffer = new byte[packet.ADD_ANIMAL.length()+1];
		
		buffer[0] = (byte)packet.ADD_ANIMAL.op();
		buffer[1] = (byte)index;
		buffer[2] = (byte)(index >>> 8);
		buffer[3] = (byte)code;
		buffer[4] = (byte)imageIndex;
		buffer[5] = (byte) x;
		buffer[6] = (byte)(x >>> 8);
		buffer[7] = (byte)(x >>> 16);
		buffer[8] = (byte) y;
		buffer[9] = (byte)(y >>> 8);
		buffer[10] = (byte)(y >>> 16);
		buffer[11] = (byte)v;
		buffer[12] = (byte)(v >>> 8);
		
		try {
			synchronized(tcpOutput) {
				tcpOutput.write(buffer, 0, packet.ADD_ANIMAL.length()+1);
				tcpOutput.flush();
			}
		} catch (IOException e) {
			System.out.println("Cannot send data to stream");
			e.printStackTrace();
			System.exit(-1);
		}
		return packet.ADD_ANIMAL.length()+1;
		
		
	}
	
	public static int removeAnimal (int index) {
		
		 byte[] buffer = new byte[packet.REMOVE_ANIMAL.length()+1];
		
		buffer[0] = (byte)packet.REMOVE_ANIMAL.op();
		buffer[1] = (byte)index;
		buffer[2] = (byte)(index >>> 8);
		
		try {
			synchronized(tcpOutput) {
				tcpOutput.write(buffer, 0, packet.REMOVE_ANIMAL.length()+1);
				tcpOutput.flush();
			}
		} catch (IOException e) {
			System.out.println("Cannot send data to stream");
			e.printStackTrace();
			System.exit(-1);
		}
		return packet.REMOVE_ANIMAL.length()+1;
	}
	
	public static int sendNewCoordinates (int index, int x, int y, float[] vector) {
		
		byte[] buffer = new byte[packet.UPDATE_COORDINATES.length()+1];
		
		buffer[0] = (byte)packet.UPDATE_COORDINATES.op();
		buffer[1] = (byte)index;
		buffer[2] = (byte)(index >>> 8);
		buffer[3] = (byte) x;
		buffer[4] = (byte)(x >>> 8);
		buffer[5] = (byte)(x >>> 16);
		buffer[6] = (byte)(x >>> 24);
		buffer[7] = (byte) y;
		buffer[8] = (byte)(y >>> 8);
		buffer[9] = (byte)(y >>> 16);
		buffer[10] = (byte)(y >>> 24);
		int v1 = Math.round((vector[0] * FLOAT_PRECISION));    //could change for precision
		int v2 = Math.round((vector[1] * FLOAT_PRECISION));
		buffer[11] = (byte)v1;
		buffer[12] = (byte)(v1 >>> 8);
		buffer[13] = (byte)(v1 >>> 16);
		buffer[14] = (byte)(v1 >>> 24);
		buffer[15] = (byte)v2;
		buffer[16] = (byte)(v2 >>> 8);
		buffer[17] = (byte)(v2 >>> 16);
		buffer[18] = (byte)(v2 >>> 24);
		
		try {
			synchronized (udpOutput) {
				udpOutput.write(buffer, 0, packet.UPDATE_COORDINATES.length()+1);
				udpOutput.flush();
			}
		} catch (IOException e) {
			System.out.println("Cannot send data to stream");
			e.printStackTrace();
			System.exit(-1);
		}
		return packet.UPDATE_COORDINATES.length()+1;
	}
	
	public static byte[] formatCoordinatesPacket (int index, int x, int y) {
		
		 byte[] buffer = new byte[packet.UPDATE_COORDINATES.length()+1];
		
		buffer[0] = (byte)packet.UPDATE_COORDINATES.op();
		buffer[1] = (byte)index;
		buffer[2] = (byte)(index >>> 8);
		buffer[3] = (byte) x;
		buffer[4] = (byte)(x >>> 8);
		buffer[5] = (byte)(x >>> 16);
		buffer[6] = (byte) y;
		buffer[7] = (byte)(y >>> 8);
		buffer[8] = (byte)(y >>> 16);
		
		return buffer;
	}
	
	public static int initializeImages (int code, int index, Color color, int width) {
		
		byte[] buffer = new byte[packet.INITIALIZE_IMAGES.length()+1];
		
		buffer[0] = (byte)packet.INITIALIZE_IMAGES.op();
		buffer[1] = (byte)code;
		buffer[2] = (byte)index;
		buffer[3] = (byte)color.getRed();
		buffer[4] = (byte)color.getGreen();
		buffer[5] = (byte)color.getBlue();
		buffer[6] = (byte)width;
		
		try {
			synchronized(tcpOutput) {
				tcpOutput.write(buffer, 0, packet.INITIALIZE_IMAGES.length()+1);
				tcpOutput.flush();
			}
		} catch (IOException e) {
			System.out.println("Cannot send data to stream");
			e.printStackTrace();
			System.exit(-1);
		}
		return packet.INITIALIZE_IMAGES.length()+1;
	}
	
	public static int sendIv () {
		
		byte[] buffer = new byte[packet.CONNECTION_INITIALIZATION.length()+1];
		Random rand = new Random();
		iv = rand.nextInt(Integer.MAX_VALUE);
		
		buffer[0] = (byte)packet.CONNECTION_INITIALIZATION.op();
		buffer[1] = (byte)iv;
		buffer[2] = (byte)(iv >>> 8);
		buffer[3] = (byte)(iv >>> 16);
		buffer[4] = (byte)(iv >>> 24);
		
		try {
			synchronized(tcpOutput) {
				tcpOutput.write(buffer, 0, packet.CONNECTION_INITIALIZATION.length()+1);
				tcpOutput.flush();
			}
		} catch (IOException e) {
			System.out.println("Cannot send data to stream");
			e.printStackTrace(); 
			System.exit(-1);
		}
		return packet.CONNECTION_INITIALIZATION.length()+1;
	}
	
	
	public static void setOutputs (PipedOutputStream tcp, PipedOutputStream udp) {
		
		tcpOutput = tcp;
		udpOutput = udp;
	}
	
}
