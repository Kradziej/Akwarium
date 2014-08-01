package logic;

import java.util.Random;

public class SwimCalc implements Runnable {
	
	
	private Thread t;
	private boolean threadRun;
	private static final int SYNCH_TIME = 30;
	

	public void run() {

		while(threadRun) {


			Random rand = new Random();
			int rNumber = rand.nextInt(100) + 1;
			int newX;
			int newY;

			// set random direction vectors //31
			float f = (rand.nextInt(maxRandomVectorX) + 1) * 0.01f;
			
			if(rNumber > leftShiftProbability) {
				float sum = vector[0] + f;
				// 1.0f
				vector[0] = sum > maxVectorX ? vector[0] - f : sum;
			} else {
				float sum = vector[0] - f;
				vector[0] = sum < -maxVectorX ? vector[0] + f : sum;
			}

			rand.setSeed(System.currentTimeMillis());
			rand.nextInt();

			

			// 17
			f = (rand.nextInt(maxRandomVectorY) + 1) * 0.01f;
			// 50
			if(rNumber > upShiftProbability) {
				// 0.25f
				float sum = vector[1] + f;
				vector[1] = sum > maxVectorY ? vector[1] - f : sum;
			} else {
				float sum = vector[1] - f;
				vector[1] = sum < -maxVectorY ? vector[1] + f : sum;
			}

			// calculate new position from velocity and vector
			// check if it's out of window
			newX = Math.round((vector[0] * v) + x);
			newY = Math.round((vector[1] * v) + y);


			/*if(newY > aq.getAquariumHeight() - distanceFromBorderBottom || distanceFromBorderTop > newY) {
				vector[1] = Math.abs(vector[1]) < 0.10f ? -(Math.abs(vector[1]) + 0.10f) : -(vector[1]/4);
				newY = Math.round((vector[1] * v) + y);
			}*/

			if(newY > aq.getAquariumHeight() - distanceFromBorderBottom)
				newY = aq.getAquariumHeight() - distanceFromBorderBottom;

			if(distanceFromBorderTop > newY)
				newY = distanceFromBorderTop;


			// flip image
			if(newX - x > 1)
				direction = 1;
			else if(newX - x < -1)
				direction = 0;

			imageChange(direction);
			x = newX;
			y = newY;


			// Conditions when on shark -> terminated
			if(aq.containsShark(this)) {
				if(aq.isServer())
					//PacketSender.sendNewCoordinates(index, 9999, y, direction);
					//reomveAnimal
				terminate();
				break;
			}

			// When out of the screen on left
			if(x < (0 - this.getImage().getWidth() - 40)) {
				if(aq.isServer())
					PacketSender.sendNewCoordinates(index, 9999, y, direction);
					//reomoveAnimal
				terminate();
				break;
			}

			if(aq.isServer())
				PacketSender.sendNewCoordinates(index, x, y, direction);


			try {
				Thread.sleep(SYNCH_TIME);
			} catch (InterruptedException e) {
				System.out.println("Thread " + Thread.currentThread().toString() + " interrupted!");
			}
		}
	}
	
	
	public void terminate () {

		threadRun = false;
	}
	
	public boolean isTerminated () {

		return !threadRun;
	}
	
	protected void startThread () {

		threadRun = true;
		t = new Thread(this);
		t.start();
	}
}
