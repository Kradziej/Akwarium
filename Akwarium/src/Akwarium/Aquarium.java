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

import Akwarium.PacketConstants.packet;

import java.awt.Graphics2D;
import java.awt.Image;

public class Aquarium extends Utility {
	
	private int waterVolume;    // mililiters?
	private int waterClarity;
	private int capacity;
	private Filter filter;
	private float boost;
	private boolean working = true;
	private Lamp lamp;
	private int indexFix;
	private DrawAq dAq;
	private Color backgroundColor = new Color(111, 181, 223);
	///private List<Animal> animals = new ArrayList<Animal>();
	private Animal[] animals = new Animal[0xFFFF];
	private int bottom = 0;
	private int top = 0;
	private int animalCount = 0;
	Random rndBoost = new Random();
	private Shark owner;   //0xFFFE
	private Shark player;  //0xFFFD
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
		
		if(top == 0xFFFC)
			top = bottom = 0;
		
		synchronized(this) {
			animals[top] = a;
			a.setIndex(top);
			top++;
		}
		
		if (isServer)
			PacketSender.addAnimal(a.getSpeciesCode(), a.getImageIndex(), a.getIndex(), a.getX(), a.getY(), a.getVelocity());
		
		a.startThread();
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
				animals[0xFFFE] = owner;
				return;
			} else {
				player = new Shark(this, false);
				player.setX(x);
				player.setY(y);
				player.setVelocity(v);
				dAq.addKeyListener(player);
				player.startThread();
				animals[0xFFFD] = player;
				return;
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
		
		if(top == 0xFFFC)
			top = bottom = 0;
		
		synchronized(this) {
			animals[index] = a;
			a.setIndex(index);
			top++;
		}
		
		a.startThread();
		animalCount++;
	}
	
	
	public boolean isWorking () {
		
		return working;
	}
	
	public void reset () {
		
		working = false;
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
		
		dAq.createBuffer();
		
		for (int i = bottom; i < top; i++) {

			if(animals[i] == null)
				continue;
		
			if(animals[i].isTerminated()) {
				synchronized(this) {
					//PacketSender.removeAnimal(i);
					animals[i] = null;
					animalCount--;
				}
				continue;
			}
				
			dAq.drawAnimal(animals[i]);
			
		}
		
		dAq.drawAnimal(owner);
		if(isMultiplayer)
			dAq.drawAnimal(player);
		
		dAq.drawBuffer();
		
		while(animals[bottom] == null && bottom < top)
			bottom++;
	}

	public DrawAq getCanvas () {
		
		return dAq;
	}
	

	public boolean containsShark (Animal a) {
		
		boolean isEatenPlayer = false;
		
		// hitbox reduction
		Rectangle2D rect = new Rectangle2D.Double(a.getX(), a.getY(), 
				a.getHitboxW(), a.getHitboxH());
		boolean isEatenOwner = rect.intersects(owner.getX(), owner.getY(), 
				owner.getHitboxW(), owner.getHitboxH());
		if(isMultiplayer)
			isEatenPlayer = rect.intersects(player.getX(), player.getY(), 
					player.getHitboxW(), player.getHitboxH());
		
		
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

	public void setBoost (float b) {
		
		this.boost = b;
	}
	
	public float boost () {
		
			//return (int)(boost * Math.abs(rndBoost.nextGaussian()));
			float base = (float)Math.abs(rndBoost.nextGaussian()) + 0.3f;
			System.out.println(boost);
			return (base += boost);
	}
	
	
	public void initSharksServer () {
		
		owner = new Shark(this, true);
		if(isMultiplayer)
			PacketSender.addAnimal(owner.getSpeciesCode(), 0, 1, owner.getX(), owner.getY(), owner.getVelocity());
		dAq.addKeyListener(owner);
		owner.startThread();
		animals[0xFFFE] = owner;
		if(isMultiplayer) {
			player = new Shark(this, false);
			PacketSender.addAnimal(player.getSpeciesCode(), 0, 0, player.getX(), 
					player.getY(), player.getVelocity());
			animals[0xFFFD] = player;
		}
	}
	
	public void initAnimalsServer() {
		
		Animal.initAnimalsServer(this);
	}
	
	public Animal getPlayer () {
		
		return player;
	}
	
	public Animal getOwner () {
		
		return owner;
	}
	
	public void updateCooridates (int index, int x, int y, int direction) {
		
		if(index == 0xFFFE) {
			owner.setX(x);
			owner.setY(y);
			owner.setDirection(direction);
			return;
		}
		else if(index == 0xFFFD) {
			player.setX(x);
			player.setY(y);
			player.setDirection(direction);
			return;
		}
		
		animals[index].setX(x);
		animals[index].setY(y);
		animals[index].setDirection(direction);
	}

	public void killAllAnimals() {
		
		for (int i = bottom; i < top; i++) {

			if(animals[i] == null)
				continue;
			
			animals[i].terminate();
		}
	}
	
	
}
	
	
	

