package Akwarium;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random; 

import javax.imageio.ImageIO;
import javax.swing.JTextArea;

import Akwarium.packetConstants.packet;

import java.awt.Graphics2D;
import java.awt.Image;

public class Aquarium extends Utility {
	
	private int waterVolume;    // mililiters?
	private int waterClarity;
	private int capacity;
	private Filter filter;
	private float shift;
	private boolean working;
	private Lamp lamp;
	private int indexFix;
	private DrawAq dAq;
	private Color backgroundColor = new Color(111, 181, 223);
	private String statusOff = "Akwarium wylaczone";
	private String statusOn = "Akwarium wlaczone";
	///private List<Animal> animals = new ArrayList<Animal>();
	private Animal[] animals = new Animal[0xFFFF];
	private int bottom = 0;
	private int top = 0;
	private int animalCount = 0;
	private Shark owner;
	private Shark player;
	private JTextArea console;
	private boolean isMultiplayer;
	private boolean isServer;  // w zaleznosci czy client czy server jeden bedzie puszczal thready drugi nie
	private boolean isClient; 
	
	
	public Aquarium (Filter filter, Lamp lamp, int capacity, JTextArea console, 
			boolean isServer, boolean isClient) {
		
		this.lamp = lamp;
		this.filter = filter;
		this.capacity = capacity;
		this.console = console;
		this.isClient = isClient;
		this.isServer = isServer;
		isMultiplayer = isClient | isServer;
		dAq = new DrawAq(this);
		dAq.setBackground(backgroundColor);
		
		// Load resources
		try {
			Animal.resources[0] = ImageIO.read(Program.class.getClass().getResource("/resources/fish.png"));
			Animal.resources[1] = ImageIO.read(Program.class.getClass().getResource("/resources/turtle.png"));
			Animal.sharkOwner = ImageIO.read(Program.class.getClass().getResource("/resources/shark.png"));
			Animal.sharkPlayer = ImageIO.read(Program.class.getClass().getResource("/resources/shark.png"));
		} catch (IOException e2) {
			System.out.println("Cannot load resources");
			e2.printStackTrace();
			System.exit(-1);
		}
	}
	
	public int getNumberOfAnimals () {
		
		return animalCount;
	}
	
	
	public void removeAnimal (int index) {
		
		synchronized(this) {
			animals[index].terminate();
			animals[index] = null;
		}
	}
	
	
	public void addAnimal () throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		Random rand = new Random();
		Animal.SpeciesList[] allSpecies = Animal.SpeciesList.values();
		
		int n = rand.nextInt(allSpecies.length);
		Animal a = (Animal) allSpecies[n].getInstance().getDeclaredConstructor(Aquarium.class).newInstance(this);
		
		if(top == 0xFFFD)
			top = bottom = 0;
		
		synchronized(this) {
			animals[top] = a;
			a.setIndex(top);
			top++;
		}
		
		if (isMultiplayer && isServer)
			packetSender.addAnimal(a.getSpeciesCode(), a.getImageIndex(), a.getIndex(), a.getX(), a.getY(), a.getVelocity());
		
		//a.startThread();
		animalCount++;
	}
	
	
	// Adding animal, function for client !!!
	public void addAnimal (int code, int imageIndex, int index, int x, int y, int v) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		if(code == Shark.CODE) {
			if(index == 1) {
				owner = new Shark(this, true);
				owner.setX(x);
				owner.setY(y);
				owner.setVelocity(v);
			} else {
				player = new Shark(this, false);
				player.setX(x);
				player.setY(y);
				player.setVelocity(v);
			}
		}
		
		Animal.SpeciesList[] allSpecies = Animal.SpeciesList.values();
		Animal a = null;
		String speciesName = null;
		
		for(Animal.SpeciesList spec : allSpecies) {
			if(spec.getOrdinal() == code) {
				a = (Animal) spec.getInstance().getDeclaredConstructor(Aquarium.class, int.class).newInstance(this, v);
				speciesName = spec.getSpeciesName();
			}
		}
		
		a.setX(x);
		a.setY(y);
		//newAnimal.setVelocity(v);
		a.setImageIndex(imageIndex);
		
		if(top == 0xFFFD)
			top = bottom = 0;
		
		synchronized(this) {
			animals[index] = a;
			a.setIndex(index);
			top++;
		}
		
		//a.startThread();
	}
	
	
	public boolean isWorking () {
		
		return working;
	}
	
	public void showStatus() {
		
		if(isWorking())
			console.setText(statusOn);
		else
			console.setText(statusOff);
	}
	
	public void start() {
		console.setText("Uruchamianie akwarium...");
		working = true;
	}
	
	public boolean fillWithWater (int water) {
		
		if(water + waterVolume >= capacity)
			return false;
		
		waterVolume += water;
		return true;
	}
	
	public boolean removeWater (int water) {
		
		if (waterVolume - water <= 0) {
			waterVolume = 0;
			return false;
		} else
			waterVolume -= water;
			return true;
	}
	
	public Animal getAnimal (int index) throws NullPointerException {
		
		return animals[index];
	}
	
	
	public Animal[] getAnimals () {
		
		return animals;
	}

	
	public int getAquariumWidth () {
		
		return dAq.getWidth();
	}
	
	public int getAquariumHeight () {
		
		return dAq.getHeight();
	}
	
	
	// depreciated !!!
	/*public void repaint () {
		
		if(bottom != top)
			dAq.redrawAnimals();
		else
			dAq.clear();
	}*/
	
	
	public void updateData () {
		

		if(top == bottom) {
			//sharks here not clear
			dAq.clear();
			return;
		}
		Image buffer = null;
		
		buffer = dAq.getBuffer();
		Graphics2D g2d = (Graphics2D)buffer.getGraphics();
		for (int i = bottom; i < top; i++) {

			if(animals[i] == null)
				continue;
			
			if(!animals[i].isStarted())
				animals[i].startThread();
			
			if(animals[i].isTerminated()) {
				synchronized(this) {
					animals[i] = null;
					//packetSender.removeAnimal(i);
					animalCount--;
				}
				continue;
			}
				
			dAq.drawAnimal(g2d, animals[i]);
			
		}
		//dAq.drawAnimal(g2d, owner);
		//dAq.drawAnimal(g2d, player);
		
		dAq.drawBuffer(buffer);
		g2d.dispose();
		
		
		while(animals[bottom] == null && bottom < top)
			bottom++;
	}

	public DrawAq getCanvas () {
		
		return dAq;
	}
	

	public boolean containsShark (Animal a) {
		
		// hitbox reduction
		Rectangle2D rect = new Rectangle2D.Double(a.getX(), a.getY(), a.getImage().getWidth() - 20, a.getImage().getHeight() - 10);
		boolean isEatenOwner = rect.intersects(owner.getX(), owner.getY(), owner.getImage().getWidth(), owner.getImage().getHeight());
		boolean isEatenPlayer = rect.intersects(player.getX(), player.getY(), player.getImage().getWidth(), player.getImage().getHeight());
		
		return isEatenOwner | isEatenPlayer;
		// set something like adding points to player or deduct health when medusa attack xD
		
	}

	public boolean isMultiplayer () {
		
		return isMultiplayer;
	}
	
	public boolean isClient () {
		
		return isClient;
	}

	public boolean isServer() {
	
		return isServer;
	}

	public void sendCoordinates (Animal a) {
		
		//sender.sendNewCoordinates(a.getIndex(), a.getX(), a.getY());
	}
	
	public void increaseShift (double shift) {
		
		shift += shift;
	}
	
	public float shift () {
		
		return shift;
	}
	
	
	public void initSharksServer () {
		
		owner = new Shark(this, true);
		packetSender.addAnimal(owner.getSpeciesCode(), 0, 1, owner.getX(), 
				owner.getY(), owner.getVelocity());
		player = new Shark(this, false);
		packetSender.addAnimal(player.getSpeciesCode(), 0, 0, player.getX(), 
				player.getY(), player.getVelocity());
	}
	
	public void initAnimalsServer() {
		
		Animal.initAnimalsServer();
	}
	
	public void updateCooridates (int index, int x, int y, float[] vector) {
		
		if(index == 0xFFFF) {
			owner.setX(x);
			owner.setX(y);
			owner.setVector(vector);
			return;
		}
		else if(index == 0xFFFE) {
			player.setY(x);
			player.setY(y);
			player.setVector(vector);
			return;
		}
		
		animals[index].setX(x);
		animals[index].setY(y);
		animals[index].setVector(vector);
	}
	
}
	
	
	

