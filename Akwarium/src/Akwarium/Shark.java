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
		this.isOwner = isOwner;
		
		if(isOwner)
			rightDirImage = copyImage(sharkOwnerImage);
		else
			rightDirImage = copyImage(sharkPlayerImage);
		
		
		leftDirImage = flipImage(copyImage(rightDirImage));
		image = rightDirImage;
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		hitboxW = imageWidth - Math.round(0.35f * imageWidth);
		hitboxH = imageHeight - Math.round(0.35f * imageHeight);
		this.setInitialCoordinates();
		v = 19;
	}
	
	public void run() {
		
		
		while(threadRun) {
			
			Random rand = new Random();
			int newX = 0;
			int newY = 0;
			
			// calculate new position from velocity and vector
			// check if it's out of window
			newX = Math.round((vector[0] * v) + x); 
			newY = Math.round((vector[1] * v) + y);
			
			
			if(newX > Aq.getAquariumWidth() - this.getImage().getWidth())
				newX = Aq.getAquariumWidth() - this.getImage().getWidth();
			
			if(0 > newX)
				newX = 0;
			
			if(newY > Aq.getAquariumHeight() - distanceFromBorderBottom)
				newY = Aq.getAquariumHeight() - distanceFromBorderBottom;
			
			if(distanceFromBorderTop > newY)
				newY = distanceFromBorderTop;
			
			float boost = 1.0f; 
			
			synchronized(this) {
				for (int k : keys) {
				switch(k) {
					case KeyEvent.VK_UP:
						vector[1] = -boost;
						break;
					case KeyEvent.VK_DOWN:
						vector[1] = boost;
						break;
					case KeyEvent.VK_RIGHT:
						vector[0] = boost;
						break;
					case KeyEvent.VK_LEFT:
						vector[0] = -boost;
						break;
					}
				}
			}
			
			// Reduce Movement
			if(keys.size() == 0) {
				vector[0] = 0;
				vector[1] = 0;
			}
			
			
			// flip image
			if(newX - x > 2) {
				this.flipImage("right");
				direction = 1;
			}
			else if(newX - x < -2) {
				this.flipImage("left");
				direction = 0;
			}

			x = newX;
			y = newY;
				
			if (Aq.isMultiplayer()) {
				if(isOwner)
					PacketSender.sharkUpdate(0, x, y, v, direction);
				else
					PacketSender.sharkUpdate(1, x, y, v, direction);
			}
			
	
			
			try {
				Thread.sleep(SYNCH_TIME);
			} catch (InterruptedException e) {
				System.out.println("Thread " + Thread.currentThread().toString() + " interrupted!");
			}
		}
		
	
	}
	public String getSpeciesName () {
		
		return species;
	}
	
	public int getSpeciesCode () {
		
		return CODE;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		
		keys.add(e.getKeyCode());
	}
	

	@Override
	public void keyReleased(KeyEvent e) {
		
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
		this.x = 3;
		this.y = rand.nextInt(Aq.getAquariumHeight() - 120) + 60;
	}
	
	public void setImageIndex(int imageIndex) {}
	
	

}
