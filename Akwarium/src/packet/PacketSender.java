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
	private ArrayBlockingQueue<PacketBlock> queue;
	private int iv;
	private boolean threadRun = true;
	private static Thread t;
	private static PacketSender packetSender;
	
	private PacketSender () {
		
		queue = new ArrayBlockingQueue<PacketBlock>(150, true);
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

		PacketBlock packet;

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
					System.out.println("Cannot send packet: " + e.getClass().toString());
					return;
				}
			}

		}
	}
	
	
	// ONLY SENDS PACKETS DO NOT SCALE X FOR EXAMPLE!!!!!!!!!!!!!!!!!!!!!!
	public int sendData (PacketConstants.Packet header, Object... args) {
		
		DataStream stream = createDataStream();

		try {
			stream.out.writeShort(header.op());   // HEADER
			for(Object o : args) {
				
				if(o.getClass() == Integer.class)
					stream.out.writeInt((int)o);
				else if(o.getClass() == Short.class)
					stream.out.writeShort((short)o);
				else if(o.getClass() == Byte.class)
					stream.out.writeByte((byte)o);
				else if(o.getClass() == Double.class)
					stream.out.writeDouble((double)o);
				else if(o.getClass() == Float.class)
					stream.out.writeFloat((float)o);
				else if(o.getClass() == Boolean.class)
					stream.out.writeBoolean((boolean)o);
			}
			stream.out.close();

		} catch (IOException e) {
			System.out.println("Cannot send data to queue for " + header.toString() + ": " + e.getClass().toString());
			return 0;
		}
		
		queue.offer(new PacketBlock(stream.toByteArray(), true));
		return stream.bytesInBuffer();
	}
	
	
	public int sendResponse (PacketConstants.Packet response) {
		
		DataStream stream = createDataStream(2);
		
		try {
			stream.out.writeShort(response.op());
			stream.out.close();
		} catch (IOException e) {
			System.out.println("Cannot send data to queue " + response.toString());
			return 0;
		}
	
		queue.offer(new PacketBlock(stream.toByteArray(), true));
		return stream.bytesInBuffer();
	}
	
	
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

