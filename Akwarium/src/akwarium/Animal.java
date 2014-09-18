package akwarium;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import resourcesLoader.Resources;

import javax.imageio.ImageIO;

import packet.PacketSender;

public abstract class Animal extends AqObject {



	protected Color c;
	protected Shape s;
	protected BufferedImage leftDirImage;
	protected BufferedImage rightDirImage;
	protected int imageIndex;
	protected Resources res;
	//protected static BufferedImage[] resources = new BufferedImage[2];
	//protected static BufferedImage[][][] graphics;    // [0]for all species [1]first is left, second right [2]number of buff images
	//protected static BufferedImage sharkOwnerImage;
	//protected static BufferedImage sharkPlayerImage;
	//protected static BufferedImage mine;
	//protected static BufferedImage blank;
	protected int hitboxW;
	protected int hitboxH;
	protected boolean clientMode;
	protected int index;
	protected Aquarium aq;
	protected Thread t;
	protected volatile boolean threadRun;
	protected boolean threadStarted;
	protected AqObjectsEnum obj;
	protected int baseWidth;
	protected int baseHeight;
	protected static int distanceFromBorderLeft = 0;
	protected static int distanceFromBorderRight = 135;
	protected static int distanceFromBorderTop = 15;
	protected static int distanceFromBorderBottom = 60;

	
	
	Animal(int baseV, AqObjectsEnum spec, float hitboxMutliplier, Aquarium aq) {
		
		obj = spec;
		Random rand = new Random();
		int index = obj.getOrdinal();
		int selectImage = rand.nextInt(obj.getNumberOfBufferedImages());
		res = Resources.getInstance();
		leftDirImage = res.getGraphics(index, 0, selectImage);
		rightDirImage = res.getGraphics(index, 1, selectImage);
		imageIndex = selectImage;
		image = rightDirImage;
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		hitboxW = imageWidth - Math.round(obj.getHitboxMultiplier() * imageWidth);
		hitboxH = imageHeight - Math.round(obj.getHitboxMultiplier() * imageHeight);
		v = (int)(baseV * DrawAq.xAnimalScale() * aq.boost());
		if(aq.isServer())
			setInitialCoordinates();
	}
	

	protected String getSpeciesName () {

		return obj.getObjectName();
	}

	
	protected int getSpeciesCode () {

		return obj.getOrdinal();
	}
	
	
	protected void setImageIndex(int imageIndex) {

		int index = obj.getOrdinal();
		leftDirImage = res.graphics[index][0][imageIndex];
		rightDirImage = res.graphics[index][1][imageIndex];
		image = rightDirImage;
	}


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
		this.x = aq.getAquariumWidth() + 60;
		this.y = rand.nextInt(aq.getAquariumHeight() - 20) + 10;
	}


	protected BufferedImage getImage () {

		return image;
	}

	public void flipImage (String dir) {

		if(dir == "left")
			image = leftDirImage;
		if(dir == "right")
			image = rightDirImage;
		if(dir == "blank")
			image = blank;
	}

	public void setIndex (int index) {

		this.index = index;
	}

	public int getIndex() {

		return index;
	}

	public static void initAnimals (Aquarium aq) {


		Color maskColor = new Color(255,255,255);
		Random rand = new Random();
		int speciesNumber = SpeciesList.values().length;
		graphics = new BufferedImage[speciesNumber][][];

		for (int i = 0; i < speciesNumber; i++) {

			graphics[i] = new BufferedImage[2][];
			graphics[i][0] = new BufferedImage[NUMBER_OF_BUFFERED_IMAGES];
			graphics[i][1] = new BufferedImage[NUMBER_OF_BUFFERED_IMAGES];

			for(int j = 0; j < NUMBER_OF_BUFFERED_IMAGES; j++) {

				BufferedImage rImg = copyImage(Animal.resources[i]);
				int width = 50 + rand.nextInt(20);

				Color c = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
				changeImageColor(rImg, maskColor, c);
				rImg = scaleImage(rImg, Math.round(width * DrawAq.xAnimalScale()));
				BufferedImage lImg = flipImage(copyImage(rImg));

				graphics[i][0][j] = lImg;
				graphics[i][1][j] = rImg;
				if(aq.isMultiplayer())
					PacketSender.initializeImages(SpeciesList.values()[i].getOrdinal(), j, c, width);

			}
		}
	}

	public static boolean initAnimalsClient (int code, int index, Color color, int width) {

		// SEND INIT TO CLIENT XD
		Color maskColor = new Color(255,255,255);
		SpeciesList[] allSpecies = SpeciesList.values();
		SpeciesList animal = null;

		for(SpeciesList spec : allSpecies) {
			if(spec.getOrdinal() == code) {
				animal = spec;
				break;
			}
		}

		int ord = animal.getOrdinal();
		if(graphics == null) {
			int speciesNumber = SpeciesList.values().length;
			graphics = new BufferedImage[speciesNumber][][];
		}
		if(graphics[ord] == null)
			graphics[ord] = new BufferedImage[2][];
		if(graphics[ord][0] == null) {
			graphics[ord][0] = new BufferedImage[NUMBER_OF_BUFFERED_IMAGES];
			graphics[ord][1] = new BufferedImage[NUMBER_OF_BUFFERED_IMAGES];
		}

		BufferedImage rImg = copyImage(resources[ord]);
		changeImageColor(rImg, maskColor, color);
		rImg = scaleImage(rImg, Math.round(width * DrawAq.xAnimalScale()));
		BufferedImage lImg = flipImage(copyImage(rImg));
		graphics[ord][0][index] = lImg;
		graphics[ord][1][index] = rImg;

		if(index == (NUMBER_OF_BUFFERED_IMAGES - 1))
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

	protected void startThread (boolean clientMode) {

		this.clientMode = clientMode;
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

		this.direction = direction;
	}

	public void imageChange (int index) {

		if(index == 0)
			flipImage("left");
		else if(index == 1)
			flipImage("right");
		else if(index == 2)
			flipImage("blank");
	}

	public static void loadResources () {

		try {
			if (resources[0] == null) {
				resources[0] = ImageIO.read(Program.class.getClass().getResource("/resources/fish.png"));
				resources[1] = ImageIO.read(Program.class.getClass().getResource("/resources/turtle.png"));
				sharkOwnerImage = ImageIO.read(Program.class.getClass().getResource("/resources/shark_blue.png"));
				sharkPlayerImage = ImageIO.read(Program.class.getClass().getResource("/resources/shark_red.png"));
				mine = ImageIO.read(Program.class.getClass().getResource("/resources/naval_mine.png"));
				blank = ImageIO.read(Program.class.getClass().getResource("/resources/blank.png"));
			}
		} catch (IOException e) {
			System.out.println("Cannot load resources");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	protected int getImageWidth () {

		return imageWidth;
	}

	protected int getImageHeight () {

		return imageHeight;
	}

	protected int getHitboxW () {

		return hitboxW;
	}

	protected int getHitboxH () {

		return hitboxH;
	}


	protected void sleepThread (int time) {

		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			System.out.println("Thread " + Thread.currentThread().toString() + " interrupted!");
		}
	}
	
	protected Dimension getNewDimensions() {
		
		Random rand = new Random();
		int width = baseWidth + rand.nextInt((int)(0.4f * baseWidth));
	}


}
