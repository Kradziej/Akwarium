package Akwarium;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;

import javax.imageio.ImageIO;

public class Shark extends Animal implements KeyListener {

	private String species = "Rekin";
	private HashSet<Integer> keys = new HashSet<Integer>();
	private int health;
	private int points;
	private boolean isOwner;
	public static final int CODE = 0xFF;
	
	Shark() {}
	
	
	Shark(Aquarium Aq, boolean isOwner) {
		
		this();
		this.Aq = Aq;
		name = "Sharky";
		this.isOwner = isOwner;
		
		if(isOwner) {
			rightDirImage = copyImage(sharkOwner);
			leftDirImage = copyImage(sharkOwner);
		} else {
			rightDirImage = copyImage(sharkPlayer);
			leftDirImage = copyImage(sharkPlayer);
		}
		
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-leftDirImage.getWidth(null), 0);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		leftDirImage = op.filter(leftDirImage, null);
		image = rightDirImage;
		this.setInitialCoordinates();
		v = 8;
	}
	
	public void run() {
		
		
		while(threadRun) {
			
			Random rand = new Random();
			int newX;
			int newY;
			
			// calculate new position from velocity and vector
			// check if it's out of window
			newX = Math.round((vector[0] * v) + x); 
			newY = Math.round((vector[1] * v) + y);
			
			
			//if(newX > Aq.getAquariumWidth() - distanceFromBorderRight || distanceFromBorderLeft > newX) {
			//	vector[0] = -(vector[0]);
			//	newX = Math.round((vector[0] * v) + x);
			//		
			//}
			
			if(newX > Aq.getAquariumWidth())
				newX = 0 - this.getImage().getWidth() + 2;
			
			if(-(this.getImage().getWidth()) > newX)
				newX = Aq.getAquariumWidth() - 2;
			
			if(newY > Aq.getAquariumHeight() - distanceFromBorderBottom) {
				//vector[1] = Math.abs(vector[1]) < 0.10f ? -(Math.abs(vector[1]) + 0.10f) : 0;
				//newY = Math.round((vector[1] * v) + y);
				newY = Aq.getAquariumHeight() - distanceFromBorderBottom;
			}
			
			if(distanceFromBorderTop > newY) {
				
				newY = distanceFromBorderTop;
			}
			
			float boost = 0.13f; 
			
			for (int k : keys) {
			switch(k) {
				case KeyEvent.VK_UP:
					vector[1] -= boost;
					break;
				case KeyEvent.VK_DOWN:
					vector[1] += boost;
					break;
				case KeyEvent.VK_RIGHT:
					vector[0] += boost;
					break;
				case KeyEvent.VK_LEFT:
					vector[0] -= boost;
					break;
				}
			}
			
			
			float reduction = 0.05f;
			float inertia = 0.06f;
			// Reduce Movement
			if (vector[0] > inertia)
				vector[0] -= reduction;
			else if(vector[0] < -inertia)
				vector[0] += reduction;
			else
				vector[0] = 0;
			
			if (vector[1] > inertia)
				vector[1] -= reduction;
			else if(vector[1] < -inertia)
				vector[1] += reduction;
			else
				vector[1] = 0;
			
			
			// flip image
			//if(newX - x > 2)
			//	this.flipImage("right");
			//else if(newX - x < -2)
			//	this.flipImage("left");

			x = newX;
			y = newY;
			
			
			if (Aq.isMultiplayer() && Aq.isServer()) {
				if(isOwner)
					PacketSender.sendNewCoordinates(0xFFFF, x, y);
				else
					PacketSender.sendNewCoordinates(0xFFFE, x, y);
			}
			
			try {
				Thread.sleep(70);
			} catch (InterruptedException e) {
				System.out.println("Thread " + Thread.currentThread().toString() + " interrupted!");
			}
		}
		
		threadRun = true;
	}
	public String getSpeciesName () {
		
		return species;
	}
	
	public int getSpeciesCode () {
		
		return CODE;
	}
	
	@Override
	public synchronized void keyPressed(KeyEvent e) {
		
		keys.add(e.getKeyCode());
	}
	

	@Override
	public synchronized void keyReleased(KeyEvent e) {
		
		keys.remove(e.getKeyCode());
	}

	@Override
	public void keyTyped(KeyEvent e) {}


	public int getHealth() {
		
		return health;
	}


	public void decreaseHealth(int minus) {
		
		health = health - minus;
	}

	public void increaseHealth(int minus) {
		
		health = health + minus;
	}

	public int getPoints() {
		
		return points;
	}


	public void addPoints(int bonus) {
		
		points = points + bonus;
	}
	
	protected void setInitialCoordinates () {
		
		Random rand = new Random();
		this.x = rand.nextInt(Aq.getAquariumWidth() - 160) + 80;
		this.y = rand.nextInt(Aq.getAquariumHeight() - 160) + 80;
	}
	
	public void setImageIndex(int imageIndex) {}

}
