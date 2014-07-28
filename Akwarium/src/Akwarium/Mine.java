package Akwarium;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.imageio.ImageIO;

public class Mine extends Animal {
	
	private float[] vector = {-0.5f,0};
	public static final int CODE = 0xFE;
	private String name = "Mine";
	private static BufferedImage image;
	private static int baseWidth = 45;
	private static int imageWidth;
	private static int imageHeight;
	private static int hitboxW;
	private static int hitboxH;

	private Mine () {

		v = (int)(3 * DrawAq.xAnimalScale());
	}
	
	
	Mine (Aquarium Aq) {
		
		this();
		this.Aq = Aq;
		this.setInitialCoordinates();
		v = Math.round(v * Aq.boost());
	}
	
	// Constructor for multiplayer
	Mine (Aquarium Aq, int v) {
			
			this.Aq = Aq;
			this.v = v;
	}
	
	Mine (String name, int x, int y, Aquarium Aq) {
		
		this();
		this.x = x;
		this.x = y;
		this.Aq = Aq;
	}
	
	Mine (int x, int y, Aquarium Aq) {
		
		this();
		this.x = x;
		this.y = y;
		this.Aq = Aq;
	}
	
	public static void initResources() {
		
		image = scaleImage(mine, Math.round(baseWidth * DrawAq.xAnimalScale()));
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		hitboxW = imageWidth - Math.round(0.4f * imageWidth);
		hitboxH = imageHeight - Math.round(0.4f * imageHeight);
	}
	
	
	public void run() {
		
		while(threadRun) {
			
			
			if(Aq.isClient()) {
				
				int notMoving = 0;
				int oldX = 0;
				int oldY = 0;
				while(threadRun) {
					
					if(oldX == x && oldY == y)
						notMoving++;
					else
						notMoving = 0;

					oldX = x;
					oldY = y;
					
					if(notMoving == 3) {
						terminate();
						break;
					}
					
					try {
						Thread.sleep(SYNCH_TIME);
					} catch (InterruptedException e) {
						System.out.println("Thread " + Thread.currentThread().toString() + " interrupted!");
					}
			
				}
				
				break;
			}
			
			
			int newX;
			int newY;
			
			// calculate new position from velocity and vector
			// check if it's out of window
			// constant movement for mines
			newX = Math.round((vector[0] * v) + x); 
			newY = Math.round((vector[1] * v) + y);
			
			
			if(newY > Aq.getAquariumHeight() - distanceFromBorderBottom)
				newY = Aq.getAquariumHeight() - distanceFromBorderBottom;
			
			if(distanceFromBorderTop > newY)
				newY = distanceFromBorderTop;
			
			
			x = newX;
			y = newY;
			
			
			// Conditions when on shark -> terminated
			if(Aq.containsShark(this)) {
				terminate();
				break;
			}
	
			// When out of the screen on left
			if(x < (0 - image.getWidth() - 40)) {
				terminate();
				break;
			}
			
			if (Aq.isServer()) {
				PacketSender.sendNewCoordinates(index, x, y, direction);
			}
			
			
			sleepThread(SYNCH_TIME);
		}
	}
	
	public String getSpeciesName () {
		
		return name;
	}
	
	public int getSpeciesCode () {
		
		return CODE;
	}
	
	public int getImageWidth () {
		
		return imageWidth;
	}

	public int getImageHeight () {
		
		return imageHeight;
	}

	public int getHitboxW () {
		
		return hitboxW;
	}

	public int getHitboxH () {
		
		return hitboxH;
	}
	
	protected BufferedImage getImage () {
		
		return image;
	}
	
	protected void setImageIndex(int imageIndex) {}
}
