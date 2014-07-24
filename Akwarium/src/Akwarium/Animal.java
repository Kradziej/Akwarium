package Akwarium;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import javax.imageio.ImageIO;

import Akwarium.Animal.SpeciesList;

public abstract class Animal extends Utility implements Runnable {
	
	public static enum SpeciesList {
		FISH("Ryba", Fish.class, 0x0), TURTLE("Zolw", Turtle.class, 0x1); 
		private String species;
		private Class<? extends Animal> instance;
		private int ordinal;
		
		SpeciesList (String s, Class<? extends Animal> iName, int ord) {
			
			species = s;
			instance = iName;
			ordinal = ord;
		}
		
		public int getOrdinal () {
			
			return ordinal;
		}
		
		public String getSpeciesName () {
			
			return species;
		}
		
		public Class<? extends Animal> getInstance () {
			
			return instance;
		}
	}
	

	protected Color c;
	protected Shape s;
	protected BufferedImage image;
	protected BufferedImage leftDirImage;
	protected BufferedImage rightDirImage;
	protected static BufferedImage[] resources = new BufferedImage[2];
	protected static BufferedImage[][][] graphics;    // [0]for all species [1]first is left, second right [2]number of buff images
	protected static BufferedImage sharkOwnerImage;
	protected static BufferedImage sharkPlayerImage;
	protected int imageWidth;
	protected int imageHeight;
	protected int hitboxW;
	protected int hitboxH;
	protected static int numberOfBufferedImages = 30;
	protected int x;
	protected int y;
	protected float[] vector = {1,0};
	protected int direction;
	protected int v;   // pixels per sec
	protected int index;
	protected Aquarium Aq;
	protected Thread t;
	protected  boolean threadRun;
	protected boolean threadStarted;
	public static SpeciesList species;
 	private int length;
	private int weight;
	protected int imageIndex;
	private Food food;
	protected static int distanceFromBorderLeft = 0;
	protected static int distanceFromBorderRight = 135;
	protected static int distanceFromBorderTop = 25;
	protected static int distanceFromBorderBottom = 100;
	protected static final int SYNCH_TIME = 30;
	
	
	
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
			
			
			
			Random rand = new Random();
			int rNumber = rand.nextInt();
			int newX;
			int newY;
			
			// set random direction vectors
			float f = (float)((rand.nextInt(31) + 1) * 0.01f);
			if(rNumber < -1073741824) {
				float sum = vector[0] + f;
				vector[0] = sum > 1.0f ? vector[0] - f : sum; 
			}
			else {
				float sum = vector[0] - f;
				vector[0] = sum < -1.0f ? vector[0] + f : sum;
			}
			
			rand.setSeed(System.currentTimeMillis());
			rand.nextInt();
			
			
			f = (float)((rand.nextInt(17) + 1) * 0.01f);
			if(rNumber < 0) {
				float sum = vector[1] + f;
				vector[1] = sum > 0.25f ? vector[1] - f : sum; 
			} else {
				float sum = vector[1] - f;
				vector[1] = sum < -0.25f ? vector[1] + f : sum; 
			}
			
			// calculate new position from velocity and vector
			// check if it's out of window
			newX = Math.round((vector[0] * v) + x); 
			newY = Math.round((vector[1] * v) + y);
			
			
			/*if(newY > Aq.getAquariumHeight() - distanceFromBorderBottom || distanceFromBorderTop > newY) {
				vector[1] = Math.abs(vector[1]) < 0.10f ? -(Math.abs(vector[1]) + 0.10f) : -(vector[1]/4);
				newY = Math.round((vector[1] * v) + y);
			}*/
			
			if(newY > Aq.getAquariumHeight() - distanceFromBorderBottom)
				newY = Aq.getAquariumHeight() - distanceFromBorderBottom;
			
			if(distanceFromBorderTop > newY)
				newY = distanceFromBorderTop;
			
			
			// flip image
			if(newX - x > 1) {
				this.flipImage("right");
				direction = 1;
			}
			else if(newX - x < -1) {
				this.flipImage("left");
				direction = 0;
			}
			
			x = newX;
			y = newY;
			
			
			// Conditions when on shark -> terminated
			if(Aq.containsShark(this)) {
				terminate();
				break;
			}
	
			// When out of the screen on left
			if(x < (0 - this.getImage().getWidth() - 40)) {
				terminate();
				break;
			}
			
			if (Aq.isServer()) {
				PacketSender.sendNewCoordinates(index, x, y, direction);
			}
			
			
			try {
				Thread.sleep(SYNCH_TIME);
			} catch (InterruptedException e) {
				System.out.println("Thread " + Thread.currentThread().toString() + " interrupted!");
			}
		}
	}
	
	protected abstract String getSpeciesName ();
	protected abstract int getSpeciesCode ();
	protected abstract void setImageIndex(int imageIndex);
	
	

	protected Color getColor() {
		return c;
	}
	
	protected void setColor (Color color) {
		
		c = color;
	}
	
	protected int getX () {
		
		return x;
	}
	
	protected int getY () {
		
		return y;
	}
	
	protected void setX (int x) {
		
		this.x = x;
	}
	
	protected void setY (int y) {
		
		this.y = y;
	}
	
	protected int getVelocity () {
		
		return v;
	}
	
	protected void setVelocity (int v) {
		
		this.v = v;
	}
	
	protected void setInitialCoordinates () {
		
		Random rand = new Random();
		this.x = Aq.getAquariumWidth() + 60;
		this.y = rand.nextInt(Aq.getAquariumHeight() - 20) + 10;
	}


	protected BufferedImage getImage () {
		
		return image;
	}
	
	public void flipImage (String dir) {
		
		if(dir == "left")
			image = leftDirImage;
		if(dir == "right")
			image = rightDirImage;
	}
	
	public void setIndex (int index) {
		
		this.index = index;
	}
	
	public int getIndex() {
		
		return index;
	}
	
	protected static void changeImageColor (BufferedImage image, Color maskColor, Color c) {
		
		
		if(maskColor.equals(c))
			return;
		
		for(int i = 0; i < image.getWidth(null); i++) {
			for(int j = 0; j < image.getHeight(null); j++) {
				
				int rgb = image.getRGB(i,j);
				if (((rgb >> 24) & 0xFF) == 0)
					continue;
				
				rgb = rgb | (0xFF << 24);
				if( rgb == maskColor.getRGB() )
					try {
						image.setRGB(i, j, c.getRGB());
					} catch(NullPointerException e) {
						e.printStackTrace();
						System.exit(0);
					}
			}
		}
	}

	
	public static BufferedImage copyImage (BufferedImage src) {
		
		BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = copy.createGraphics();
		g.drawImage(src, 0, 0, null);
		g.dispose();
		return copy;
	}
	
	public static BufferedImage flipImage (BufferedImage img) {
		
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-img.getWidth(null), 0);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		return op.filter(img, null);
	}
	
	public static BufferedImage scaleImage (BufferedImage img, int width) {
		
		int height = (int)(width * ((double)img.getHeight()/img.getWidth()));
		BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = buff.createGraphics();
		g.drawImage(img.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
		g.dispose();
		return buff;
	}
	
	public static void initAnimals (Aquarium Aq) {
		
		
		Color maskColor = new Color(255,255,255);
		Random rand = new Random();
		int speciesNumber = SpeciesList.values().length;
		graphics = new BufferedImage[speciesNumber][][];
		
		for (int i = 0; i < speciesNumber; i++) {
			
			graphics[i] = new BufferedImage[2][];
			graphics[i][0] = new BufferedImage[numberOfBufferedImages];
			graphics[i][1] = new BufferedImage[numberOfBufferedImages];
			
			for(int j = 0; j < numberOfBufferedImages; j++) {
			
					BufferedImage rImg = Animal.copyImage(Animal.resources[i]);
					int width = 70 + rand.nextInt(40);
					
					Color c = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
					Animal.changeImageColor(rImg, maskColor, c);
					rImg = Animal.scaleImage(rImg, Math.round(width * DrawAq.animalScale()));
					BufferedImage lImg = Animal.flipImage(Animal.copyImage(rImg));
					
					graphics[i][0][j] = lImg;
					graphics[i][1][j] = rImg;
					if(Aq.isMultiplayer())
						PacketSender.initializeImages(SpeciesList.values()[i].getOrdinal(), j, c, width);
					
			}
		}
	}
	
	public static boolean initAnimalsClient (int code, int index, Color color, int width) {
		
		// SEND INIT TO CLIENT XD
		Color maskColor = new Color(255,255,255);
		Animal.SpeciesList[] allSpecies = Animal.SpeciesList.values();
		Animal.SpeciesList animal = null;
		
		for(Animal.SpeciesList spec : allSpecies) {
			if(spec.getOrdinal() == code) {
				animal = spec;
				break;
			}
		}
		
		int ord = animal.getOrdinal();
		if(Animal.graphics == null) {
			int speciesNumber = SpeciesList.values().length;
			Animal.graphics = new BufferedImage[speciesNumber][][];
		}
		if(Animal.graphics[ord] == null)
			Animal.graphics[ord] = new BufferedImage[2][];
		if(Animal.graphics[ord][0] == null) {
			Animal.graphics[ord][0] = new BufferedImage[numberOfBufferedImages];
			Animal.graphics[ord][1] = new BufferedImage[numberOfBufferedImages];
		}
		
		BufferedImage rImg = Animal.copyImage(Animal.resources[ord]);
		Animal.changeImageColor(rImg, maskColor, color);
		rImg = Animal.scaleImage(rImg, Math.round(width * DrawAq.animalScale()));
		BufferedImage lImg = Animal.flipImage(Animal.copyImage(rImg));
		Animal.graphics[ord][0][index] = lImg;
		Animal.graphics[ord][1][index] = rImg;
		
		if(index == (numberOfBufferedImages - 1))
			return true;
		
		return false;
	}
	
	public boolean isTerminated () {

		return !threadRun;
	}
	
	protected void terminate () {
		
		threadRun = false;
	}
	
	protected boolean isStarted () {
		
		return threadStarted;
	}
	
	protected void startThread () {
		
		threadRun = true;
		threadStarted = true;
		t = new Thread(this);
		t.start();
	}
	
	public int getImageIndex () {
		
		return imageIndex;
	}
	
	public void setVector (float[] vector) {
		
		this.vector = vector;
	}

	public void setDirection (int direction) {
		
		if(direction == 0)
			flipImage("left");
		else
			flipImage("right");
	}

	public static void loadResources () {
		
		try {
			if (resources[0] == null) {
				resources[0] = ImageIO.read(Program.class.getClass().getResource("/resources/fish.png"));
				resources[1] = ImageIO.read(Program.class.getClass().getResource("/resources/turtle.png"));
				sharkOwnerImage = ImageIO.read(Program.class.getClass().getResource("/resources/shark_blue.png"));
				sharkPlayerImage = ImageIO.read(Program.class.getClass().getResource("/resources/shark_red.png"));
			}
		} catch (IOException e2) {
			System.out.println("Cannot load resources");
			e2.printStackTrace();
			System.exit(-1);
		}
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
	

	

}
