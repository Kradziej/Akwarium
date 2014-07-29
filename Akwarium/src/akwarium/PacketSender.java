package akwarium;

import java.awt.Color;
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

		byte[] buffer = new byte[packet.ADD_ANIMAL.length()+1];

		// scale
		x = Math.round(x * DrawAq.xScale());
		y = Math.round(y * DrawAq.yScale());

		buffer[0] = (byte)packet.ADD_ANIMAL.op();
		buffer[1] = (byte)index;
		buffer[2] = (byte)(index >>> 8);
		buffer[3] = (byte)code;
		buffer[4] = (byte)imageIndex;
		buffer[5] = (byte) x;
		buffer[6] = (byte)(x >>> 8);
		buffer[7] = (byte)(x >>> 16);
		buffer[8] = (byte)(x >>> 24);
		buffer[9] = (byte) y;
		buffer[10] = (byte)(y >>> 8);
		buffer[11] = (byte)(y >>> 16);
		buffer[12] = (byte)(y >>> 24);
		buffer[13] = (byte)v;
		buffer[14] = (byte)(v >>> 8);


		/*
		try {
			synchronized(tcpOutput) {
				tcpOutput.write(buffer, 0, packet.ADD_ANIMAL.length()+1);
				tcpOutput.flush();
			}
		} catch (IOException e) {
			System.out.println("Cannot send data to stream for " + packet.ADD_ANIMAL.toString());
			return 0;
		}*/

		queue.offer(new packetBlock(buffer, true));
		return packet.ADD_ANIMAL.length()+1;

	}

	public static int removeAnimal (int index) {

		byte[] buffer = new byte[packet.REMOVE_ANIMAL.length()+1];

		buffer[0] = (byte)packet.REMOVE_ANIMAL.op();
		buffer[1] = (byte)index;
		buffer[2] = (byte)(index >>> 8);

		/*
		try {
			synchronized(tcpOutput) {
				tcpOutput.write(buffer, 0, packet.REMOVE_ANIMAL.length()+1);
				tcpOutput.flush();
			}
		} catch (IOException e) {
			System.out.println("Cannot send data to stream");
			return 0;
		}*/

		queue.offer(new packetBlock(buffer, true));
		return packet.REMOVE_ANIMAL.length()+1;
	}

	public static int sendNewCoordinates (int index, int x, int y, int direction) {

		byte[] buffer = new byte[packet.UPDATE_COORDINATES.length()+1];

		// scale
		x = Math.round(x * DrawAq.xScale());
		y = Math.round(y * DrawAq.yScale());

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
		buffer[11] = (byte)direction;

		/*
		try {
			synchronized (udpOutput) {
				udpOutput.write(buffer, 0, packet.UPDATE_COORDINATES.length()+1);
				udpOutput.flush();
			}
		} catch (IOException e) {
			System.out.println("Cannot send data to stream for " + packet.UPDATE_COORDINATES.toString());
			e.printStackTrace();
			System.exit(-1);
			return 0;
		}*/

		queue.offer(new packetBlock(buffer, false));
		return packet.UPDATE_COORDINATES.length()+1;
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

		/*
		try {
			synchronized(tcpOutput) {
				tcpOutput.write(buffer, 0, packet.INITIALIZE_IMAGES.length()+1);
				tcpOutput.flush();
			}
		} catch (IOException e) {
			System.out.println("Cannot send data to stream for " + packet.INITIALIZE_IMAGES.toString());
			return 0;
		}*/


		queue.offer(new packetBlock(buffer, true));
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

		/*
		try {
			synchronized(tcpOutput) {
				tcpOutput.write(buffer, 0, packet.CONNECTION_INITIALIZATION.length()+1);
				tcpOutput.flush();
			}
		} catch (IOException e) {
			System.out.println("Cannot send data to stream for " + packet.CONNECTION_INITIALIZATION.toString());
			return 0;
		}*/

		queue.offer(new packetBlock(buffer, true));
		return packet.CONNECTION_INITIALIZATION.length()+1;
	}

	public static int sharkUpdate (int index, int x, int y, int v, int direction) {

		byte[] buffer = new byte[packet.SHARK_UPDATE.length()+1];

		if(index == 0) {  // owner coordinates send to the client
			x = Math.round(x * DrawAq.xScale());
			y = Math.round(y * DrawAq.yScale());
		}

		buffer[0] = (byte)packet.SHARK_UPDATE.op();
		buffer[1] = (byte)index;
		buffer[2] = (byte) x;
		buffer[3] = (byte)(x >>> 8);
		buffer[4] = (byte)(x >>> 16);
		buffer[5] = (byte)(x >>> 24);
		buffer[6] = (byte) y;
		buffer[7] = (byte)(y >>> 8);
		buffer[8] = (byte)(y >>> 16);
		buffer[9] = (byte)(y >>> 24);
		buffer[10] = (byte)v;
		buffer[11] = (byte)(v >>> 8);
		buffer[12] = (byte)direction;

		/*
		try {
			synchronized (udpOutput) {
				udpOutput.write(buffer, 0, packet.SHARK_UPDATE.length()+1);
				udpOutput.flush();
			}
		} catch (IOException e) {
			System.out.println("Cannot send data to stream for " + packet.SHARK_UPDATE.toString());
			return 0;
		}*/

		queue.offer(new packetBlock(buffer, false));
		return packet.SHARK_UPDATE.length()+1;
	}


	public static int updatePoints (int index, int points, int health) {

		byte[] buffer = new byte[packet.UPDATE_POINTS.length()+1];


		buffer[0] = (byte)packet.UPDATE_POINTS.op();
		buffer[1] = (byte)index;
		buffer[2] = (byte)points;
		buffer[3] = (byte)(points >>> 8);
		buffer[4] = (byte)(points >>> 16);
		buffer[5] = (byte)(points >>> 24);
		buffer[6] = (byte)health;


		/*
		try {
			synchronized (udpOutput) {
				udpOutput.write(buffer, 0, packet.UPDATE_POINTS.length()+1);
				udpOutput.flush();
			}
		} catch (IOException e) {
			System.out.println("Cannot send data to stream for " + packet.UPDATE_POINTS.toString());
			return 0;
		}*/

		queue.offer(new packetBlock(buffer, false));
		return packet.UPDATE_POINTS.length()+1;
	}


	public static void initSender (PipedOutputStream tcp, PipedOutputStream udp) {

		tcpOutput = tcp;
		udpOutput = udp;
		queue = new ArrayBlockingQueue<packetBlock>(150, true);
	}

}
