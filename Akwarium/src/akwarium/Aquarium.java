package akwarium;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import packet.PacketSender;

public class Aquarium {

	private float boost;
	private boolean working = true;
	private DrawAq dAq;
	private Color backgroundColor = new Color(111, 181, 223);
	///private List<Animal> animals = new ArrayList<Animal>();
	private Animal[] animals = new Animal[0xFFFF];
	private int bottom = 0;
	private int top = 0;
	private int animalCount = 0;
	private StatusPanel status;
	private Random rndBoost = new Random();
	private Shark owner;   //0xFFFE
	private Shark player;  //0xFFFD
	private boolean isMultiplayer;
	private boolean isServer;  // w zaleznosci czy client czy server jeden bedzie puszczal thready drugi nie
	private boolean isClient;
	private static Aquarium instance;


	private Aquarium (boolean isServer, boolean isClient, StatusPanel status) {

		this.isClient = isClient;
		this.isServer = isServer;
		isMultiplayer = isClient | isServer;
		dAq = new DrawAq(this);
		dAq.setBackground(backgroundColor);
		this.status = status;
	}

	public static Aquarium getInstance (boolean isServer, boolean isClient, StatusPanel status) {

		if(instance == null) {
			instance = new Aquarium(isServer, isClient, status);
		}

		return instance;
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

	public void addAnimal (Animal a) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		if(top == 0xFFFC)
			top = bottom = 0;

		synchronized(this) {
			animals[top] = a;
			a.setIndex(top);
			top++;
		}

		if (isServer)
			PacketSender.addAnimal(a.getSpeciesCode(), a.getImageIndex(), a.getIndex(), a.getX(), a.getY(), a.getVelocity());

		a.startThread(false);
		animalCount++;
	}

	public void addAnimal () throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		Random rand = new Random();
		Animal.SpeciesList[] allSpecies = Animal.SpeciesList.values();

		int n = rand.nextInt(allSpecies.length);
		Animal a = allSpecies[n].getInstance().getDeclaredConstructor(Aquarium.class).newInstance(this);

		if(top == 0xFFFC)
			top = bottom = 0;

		synchronized(this) {
			animals[top] = a;
			a.setIndex(top);
			top++;
		}

		if(isServer)
			PacketSender.addAnimal(a.getSpeciesCode(), a.getImageIndex(), a.getIndex(), a.getX(), a.getY(), a.getVelocity());

		a.startThread(false);
		animalCount++;
	}


	// Adding animal, function for client !!!
	public void addAnimal (int code, int imageIndex, int index, int x, int y, int v) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		Animal.SpeciesList[] allSpecies = Animal.SpeciesList.values();
		Animal a = null;
		String speciesName = null;

		for(Animal.SpeciesList spec : allSpecies) {
			if(code == spec.getOrdinal()) {
				a = spec.getInstance().getDeclaredConstructor(Aquarium.class, int.class).newInstance(this, v);
				break;
			} else if(code == Mine.CODE) {
				a = new Mine(this,v);
				break;
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

		a.startThread(true);
		animalCount++;
	}


	public boolean isWorking () {

		return working;
	}

	public void reset () {

		working = false;
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
		boolean isEatenOwner = false;

		// hitbox reduction
		Rectangle2D rect = new Rectangle2D.Double(a.getX(), a.getY(),
				a.getHitboxW(), a.getHitboxH());
		if(!owner.ishpLost())
			isEatenOwner = rect.intersects(owner.getX(), owner.getY(),
					owner.getHitboxW(), owner.getHitboxH());
		if(isMultiplayer && !owner.ishpLost())
			isEatenPlayer = rect.intersects(player.getX(), player.getY(),
					player.getHitboxW(), player.getHitboxH());


		// set something like adding points to player or deduct health
		if(isEatenOwner) {
			owner.addPoints( (int)((owner.getImageWidth() / (double)a.getImageWidth()) * (boost+2)) );
			status.setPointsOwner(owner.getPoints());
			// Mine or something else
			if(a.getSpeciesCode() == Mine.CODE) {
				owner.decreaseHealth(10);
				status.setHealthMultiplierOwner((double)owner.getHealth() / Shark.MAX_HEALTH);
				owner.specEffHealthDecrease();
			}
			if(isMultiplayer)
				PacketSender.updatePoints(0, owner.getPoints(), owner.getHealth());

		} else if(isEatenPlayer) {
			player.addPoints( (int)((player.getImageWidth() / (double)a.getImageWidth()) * (boost+2)) );
			status.setPointsPlayer(player.getPoints());
			// Mine or something else
			if(a.getSpeciesCode() == Mine.CODE) {
				player.decreaseHealth(10);
				status.setHealthMultiplierPlayer((double)player.getHealth() / Shark.MAX_HEALTH);
				player.specEffHealthDecrease();
			}
			if(isMultiplayer)
				PacketSender.updatePoints(1, player.getPoints(), player.getHealth());
		}

		return isEatenOwner | isEatenPlayer;
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
		//System.out.println(b);
	}

	public float boost () {

		float base = (float)Math.abs(rndBoost.nextGaussian()) + 0.3f;
		return (base += boost);
	}


	public void initSharks () {

		if(isMultiplayer) {
			owner = new Shark(this, true);
			player = new Shark(this, false);
			if(isClient) {
				dAq.addKeyListener(player);
				player.startThread(false);
			} else {
				dAq.addKeyListener(owner);
				owner.startThread(false);
			}
		} else {
			owner = new Shark(this, true);
			dAq.addKeyListener(owner);
			owner.startThread(false);
		}

	}

	public void initAnimals() {

		Animal.initAnimals(this);
	}

	public Shark getPlayer () {

		return player;
	}

	public Shark getOwner () {

		return owner;
	}

	public void updatePoints (int index, int points, int health) {

		if(index == 0) {

			if(owner.getHealth() != health) {
				owner.setHealth(health);
				status.setHealthMultiplierOwner((double)health / Shark.MAX_HEALTH);
				owner.specEffHealthDecrease();
			}
			if(owner.getPoints() != points) {
				owner.setPoints(points);
				status.setPointsOwner(points);
			}

		} else {

			if(player.getHealth() != health) {
				player.setHealth(health);
				status.setHealthMultiplierPlayer((double)health / Shark.MAX_HEALTH);
				player.specEffHealthDecrease();
			}
			if(player.getPoints() != points) {
				player.setPoints(points);
				status.setPointsPlayer(points);
			}

		}
	}

	public void updateSharks (int index, int x, int y, int v, int direction) {

		if(index == 0) {

			owner.setX(x);
			owner.setY(y);
			owner.setVelocity(v);
			owner.imageChange(direction);

		} else {

			player.setX(x);
			player.setY(y);
			player.setVelocity(v);
			player.imageChange(direction);
		}
	}

	public void updateCooridates (int index, int x, int y, int direction) {

		animals[index].setX(x);
		animals[index].setY(y);
		animals[index].imageChange(direction);
	}

	public void killAllAnimals() {

		for (int i = bottom; i < top; i++) {

			if(animals[i] == null)
				continue;

			animals[i].terminate();
		}
		owner.terminate();
		player.terminate();
	}


}




