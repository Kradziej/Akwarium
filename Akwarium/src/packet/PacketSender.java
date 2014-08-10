package packet;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import akwarium.DrawAq;
import static packet.PacketConstants.*;

public class PacketSender implements Runnable {

	private OutputStream tcpOut;
	private OutputStream udpOut;
	private ArrayBlockingQueue<packetBlock> queue;
	private int iv;
	private boolean threadRun = true;
	private static Thread t;
	private static PacketSender packetSender;
	
	private PacketSender () {
		
		queue = new ArrayBlockingQueue<packetBlock>(150, true);
	}
	
	
	public static PacketSender getSender () {
		
		if(packetSender == null) {
			packetSender = new PacketSender();
			t = new Thread(packetSender);
			t.start();
		}
		
		return packetSender;
	}
	
	public void addTcpOutput (OutputStream tcp) {
		this.tcpOut = tcp;
	}
	
	public void addUdpOutput (OutputStream udp) {
		this.udpOut = udp;
	}
	
	protected void terminate () {

		queue.clear();
		threadRun = false;
	}

	@Override
	public void run() {

		packetBlock packet;

		while(threadRun) {
			
			try {
				packet = queue.take();
			} catch (InterruptedException e) {
				System.out.println("Queue interrupted!");
				e.printStackTrace();
				return;
			}

			try {

				if(packet.isTcpPacket()) {

					tcpOut.write(packet.getData());
				} else {

					udpOut.write(packet.getData());
				}

			} catch (IOException | NullPointerException e) {
				if(e.getClass() == NullPointerException.class) {
					System.out.println( (packet.isTcpPacket() ? "TCP" : "UDP") + " output not yet initialized!" );
					return;
				} else {
					System.out.println("Cannot send packet");
					e.printStackTrace();
					return;
				}
			}

		}
	}
	
	
	
	
	
	// ONLY SENDS PACKETS DO NOT SCALE X FOR EXAMPLE!!!!!!!!!!!!!!!!!!!!!!
	public int sendData (PacketConstants.packet header, Object... args) {
		
		DataStream stream = createDataStream();

		try {
			stream.out.writeShort(header.op());   // HEADER
			for(Object o : args) {
				
				if(o.getClass() == Integer.class)
					stream.out.writeInt((Integer)o);
				else if(o.getClass() == Short.class)
					stream.out.writeShort((Short)o);
				else if(o.getClass() == Byte.class)
					stream.out.writeByte((Byte)o);
				else if(o.getClass() == Double.class)
					stream.out.writeDouble((Double)o);
				else if(o.getClass() == Float.class)
					stream.out.writeFloat((Float)o);
				else if(o.getClass() == Boolean.class)
					stream.out.writeBoolean((Boolean)o);
			}
			stream.out.close();

		} catch (IOException e) {
			System.out.println("Cannot send data to queue for " + header.toString());
			return 0;
		}
		
		queue.offer(new packetBlock(stream.toByteArray(), true));
		return stream.bytesInBuffer();
	}
	
	
	public int sendResponse (PacketConstants.packet response) {
		
		DataStream stream = createDataStream(2);
		
		try {
			stream.out.writeShort(response.op());
			stream.out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + response.toString());
			return 0;
		}
	
		queue.offer(new packetBlock(stream.toByteArray(), false));
		return stream.bytesInBuffer();
	}
	
	
	
	
	
	
	
	
	/*
	
	
	
	

	public int addAnimal (int code, int imageIndex, int index, int x, int y, int v) {

		DataStream stream = createDataStream();

		// scale
		x = Math.round(x * DrawAq.xScale());
		y = Math.round(y * DrawAq.yScale());
		
		try {
			stream.out.writeShort(packet.ADD_ANIMAL.op());   // DOUBLE PACKET HEADERS
			stream.out.writeShort(index);
			stream.out.writeByte(code);
			stream.out.writeByte(imageIndex);
			stream.out.writeInt(x);
			stream.out.writeInt(y);
			stream.out.writeShort(v);
			stream.out.close();

		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.ADD_ANIMAL.toString());
			return 0;
		}
		
		queue.offer(new packetBlock(stream.toByteArray(), true));
		return stream.bytesInBuffer();

	}

	public int removeAnimal (int index) {

		DataStream stream = createDataStream();

		try {
			stream.out.writeShort(packet.REMOVE_ANIMAL.op());
			stream.out.writeShort(index);
			stream.out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.REMOVE_ANIMAL.toString());
			return 0;
		}

		queue.offer(new packetBlock(stream.toByteArray(), true));
		return stream.bytesInBuffer();
	}

	public int sendNewCoordinates (int index, int x, int y, int direction) {

		DataStream stream = createDataStream();

		// scale
		x = Math.round(x * DrawAq.xScale());
		y = Math.round(y * DrawAq.yScale());
		
		
		try {
			stream.out.writeShort(packet.UPDATE_COORDINATES.op());
			stream.out.writeShort(index);
			stream.out.writeInt(x);
			stream.out.writeInt(y);
			stream.out.writeByte(direction);
			stream.out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.UPDATE_COORDINATES.toString());
			return 0;
		}
		
		queue.offer(new packetBlock(stream.toByteArray(), false));
		return stream.bytesInBuffer();
	}


	public int initializeImages (int code, int index, int width, Color color) {

		DataStream stream = createDataStream();
		
		try {
			stream.out.writeShort(packet.INITIALIZE_IMAGES.op());
			stream.out.writeByte(code);
			stream.out.writeByte(index);
			stream.out.writeByte(color.getRed());
			stream.out.writeByte(color.getGreen());
			stream.out.writeByte(color.getBlue());
			stream.out.writeByte(width);   // fix this to larger!~!!!!!!!!!!!!!!!!!
			stream.out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.INITIALIZE_IMAGES.toString());
			return 0;
		}
		
		queue.offer(new packetBlock(stream.toByteArray(), true));
		return stream.bytesInBuffer();
	}

	public int sendIv () {

		DataStream stream = createDataStream();
		
		Random rand = new Random();
		iv = rand.nextInt(Integer.MAX_VALUE);

		try {
			stream.out.writeShort(packet.CONNECTION_INITIALIZATION.op());
			stream.out.writeInt(iv);
			stream.out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.CONNECTION_INITIALIZATION.toString());
			return 0;
		}
		
		
		queue.offer(new packetBlock(stream.toByteArray(), true));
		return stream.bytesInBuffer();
	}

	public int playerUpdate (int index, int x, int y, int v, int direction) {

		DataStream stream = createDataStream();
		
		if(index == 0) {  // owner coordinates send to the client
			x = Math.round(x * DrawAq.xScale());
			y = Math.round(y * DrawAq.yScale());
		}

		try {
			stream.out.writeShort(packet.UPDATE_PLAYERS.op());
			stream.out.writeByte(index);
			stream.out.writeInt(x);
			stream.out.writeInt(y);
			stream.out.writeShort(v);
			stream.out.writeShort(direction);
			stream.out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.UPDATE_PLAYERS.toString());
			return 0;
		}

		queue.offer(new packetBlock(stream.toByteArray(), false));
		return stream.bytesInBuffer();
	}


	public int updatePoints (int index, int points, int health) {
		
		DataStream stream = createDataStream();
		
		try {
			stream.out.writeShort(packet.UPDATE_POINTS.op());
			stream.out.writeByte(index);
			stream.out.writeInt(points);
			stream.out.writeByte(health);
			stream.out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.UPDATE_PLAYERS.toString());
			return 0;
		}
		
		queue.offer(new packetBlock(stream.toByteArray(), false));
		return stream.bytesInBuffer();
	}
	
	public int imagesInitializationEnd () {
		
		DataStream stream = createDataStream();
		
		try {
			stream.out.writeShort(packet.IMAGES_INIT_END.op());
			stream.out.writeBoolean(true);
			stream.out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + packet.IMAGES_INIT_END.toString());
			return 0;
		}
	
		queue.offer(new packetBlock(stream.toByteArray(), false));
		return stream.bytesInBuffer();
	}
	
	
	
	*/
	
	
	
	
	
	private DataStream createDataStream (int size) {
		
		DataStream stream = new DataStream(size);
		return stream;
	}

	private DataStream createDataStream () {
		
		DataStream stream = new DataStream(MAX_PACKET_LENGTH);
		return stream;
	}
	
	private class DataStream {
		
		ByteArrayOutputStream bytesBuffer;
		DataOutputStream out;
		
		DataStream(int size) {
			bytesBuffer = new ByteArrayOutputStream(size);
			out = new DataOutputStream(bytesBuffer);
		}
		
		byte[] toByteArray () {
			return bytesBuffer.toByteArray();
		}
		
		int bytesInBuffer () {
			return bytesBuffer.size();
		}
	}
	
}

