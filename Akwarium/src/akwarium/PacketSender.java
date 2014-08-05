package akwarium;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PacketSender extends Thread implements PacketConstants {

	private static PipedOutputStream tcpOutput;
	private static PipedOutputStream udpOutput;
	private static ArrayBlockingQueue<packetBlock> queue;
	private static int iv;
	private static boolean threadRun = true;

	@Override
	public void run() {

		packetBlock packet;

		while(threadRun) {
			try {
				packet = queue.poll(30, TimeUnit.MICROSECONDS);
				if(packet == null)
					continue;
			} catch (InterruptedException e) {
				System.out.println("Queue interrapted!");
				e.printStackTrace();
				return;
			}

			try {

				if(packet.isTcpPacket()) {

					tcpOutput.write(packet.getBuffer(), 0, packet.size());
					tcpOutput.flush();
				} else {

					udpOutput.write(packet.getBuffer(), 0, packet.size());
					udpOutput.flush();
				}

			} catch (IOException e) {
				System.out.println("Cannot send data to stream");
				e.printStackTrace();
				return;
			}

		}
	}

	protected static void terminate () {

		threadRun = false;
	}


	public static int addAnimal (int code, int imageIndex, int index, int x, int y, int v) {

		ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(64);
		DataOutputStream out = new DataOutputStream(bytesBuffer);

		// scale
		x = Math.round(x * DrawAq.xScale());
		y = Math.round(y * DrawAq.yScale());
		
		try {
			out.writeShort(packet.ADD_ANIMAL.op());   // DOUBLE PACKET HEADERS
			out.writeShort(index);
			out.writeByte(code);
			out.writeByte(imageIndex);
			out.writeInt(x);
			out.writeInt(y);
			out.writeShort(v);
			out.close();

		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.ADD_ANIMAL.toString());
			return 0;
		}
		
		queue.offer(new packetBlock(bytesBuffer.toByteArray(), true));
		return bytesBuffer.size();

	}

	public static int removeAnimal (int index) {

		ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(64);
		DataOutputStream out = new DataOutputStream(bytesBuffer);

		try {
			out.writeShort(packet.REMOVE_ANIMAL.op());
			out.writeShort(index);
			out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.REMOVE_ANIMAL.toString());
			return 0;
		}

		queue.offer(new packetBlock(bytesBuffer.toByteArray(), true));
		return bytesBuffer.size();
	}

	public static int sendNewCoordinates (int index, int x, int y, int direction) {

		ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(64);
		DataOutputStream out = new DataOutputStream(bytesBuffer);

		// scale
		x = Math.round(x * DrawAq.xScale());
		y = Math.round(y * DrawAq.yScale());
		
		
		try {
			out.writeShort(packet.UPDATE_COORDINATES.op());
			out.writeShort(index);
			out.writeInt(x);
			out.writeInt(y);
			out.writeByte(direction);
			out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.UPDATE_COORDINATES.toString());
			return 0;
		}
		
		queue.offer(new packetBlock(bytesBuffer.toByteArray(), false));
		return bytesBuffer.size();
	}


	public static int initializeImages (int code, int index, int width, Color color) {

		ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(64);
		DataOutputStream out = new DataOutputStream(bytesBuffer);
		
		try {
			out.writeShort(packet.INITIALIZE_IMAGES.op());
			out.writeByte(code);
			out.writeByte(index);
			out.writeByte(color.getRed());
			out.writeByte(color.getGreen());
			out.writeByte(color.getBlue());
			out.writeByte(width);   // fix this to larger!~!!!!!!!!!!!!!!!!!
			out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.INITIALIZE_IMAGES.toString());
			return 0;
		}
		
		queue.offer(new packetBlock(bytesBuffer.toByteArray(), true));
		return bytesBuffer.size();
	}

	public static int sendIv () {

		ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(64);
		DataOutputStream out = new DataOutputStream(bytesBuffer);
		
		Random rand = new Random();
		iv = rand.nextInt(Integer.MAX_VALUE);

		try {
			out.writeShort(packet.CONNECTION_INITIALIZATION.op());
			out.writeInt(iv);
			out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.CONNECTION_INITIALIZATION.toString());
			return 0;
		}
		
		
		queue.offer(new packetBlock(bytesBuffer.toByteArray(), true));
		return bytesBuffer.size();
	}

	public static int playerUpdate (int index, int x, int y, int v, int direction) {

		ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(64);
		DataOutputStream out = new DataOutputStream(bytesBuffer);

		if(index == 0) {  // owner coordinates send to the client
			x = Math.round(x * DrawAq.xScale());
			y = Math.round(y * DrawAq.yScale());
		}

		try {
			out.writeShort(packet.UPDATE_PLAYERS.op());
			out.writeByte(index);
			out.writeInt(x);
			out.writeInt(y);
			out.writeShort(v);
			out.writeShort(direction);
			out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.UPDATE_PLAYERS.toString());
			return 0;
		}

		queue.offer(new packetBlock(bytesBuffer.toByteArray(), false));
		return bytesBuffer.size();
	}


	public static int updatePoints (int index, int points, int health) {
		
		ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(64);
		DataOutputStream out = new DataOutputStream(bytesBuffer);
		
		try {
			out.writeShort(packet.UPDATE_POINTS.op());
			out.writeByte(index);
			out.writeInt(points);
			out.writeByte(health);
			out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.UPDATE_PLAYERS.toString());
			return 0;
		}
		
		queue.offer(new packetBlock(bytesBuffer.toByteArray(), false));
		return bytesBuffer.size();
	}
	
	public static int imagesInitializationEnd() {
		
		ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(8);
		DataOutputStream out = new DataOutputStream(bytesBuffer);
		
		try {
			out.writeShort(packet.IMAGES_INIT_END.op());
			out.writeBoolean(true);
			out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.IMAGES_INIT_END.toString());
			return 0;
		}
	
		queue.offer(new packetBlock(bytesBuffer.toByteArray(), false));
		return bytesBuffer.size();
	}


	public static void init (PipedOutputStream tcp, PipedOutputStream udp) {

		tcpOutput = tcp;
		udpOutput = udp;
		queue = new ArrayBlockingQueue<packetBlock>(150, true);
	}

	

}

