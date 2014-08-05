package akwarium;

import java.awt.Dimension;

public enum AqObjectsEnum {
	
	// Remove class Fish Turtle, use only Obstacles i Animals
	// Fix dimensions
	// zrob buildera zamiast refleksji
	FISH("fish", Animal.class, 8, 0.45f, new Dimension(50,20), 0x00, 30), 
	TURTLE("turtle", Animal.class, 6, 0.45f, new Dimension(50,20), 0x01, 30),
	MINE("naval_mine", Obstacles.class, 3, 0.4f, new Dimension(50, 20), 0x02, 1),
	OWNER("shark_blue", Shark.class, 19, 0.3f, new Dimension(160, 100), 0x03, 1),
	PLAYER("shark_red", Shark.class, 19, 0.3f, new Dimension(160, 100), 0x04, 1);
	
	
	public static final AqObjectsEnum[] CONTROLLABLE = {OWNER, PLAYER};
	public static final AqObjectsEnum[] UNCONTROLLABLE = {FISH, TURTLE, MINE};
	private String species;
	private Class<?> instance;
	private int ordinal;
	private int baseV;
	private float hitboxMultiplier;
	private Dimension baseDim;
	private int numberOfBufferedImages;

	AqObjectsEnum (String s, Class<?> iName, int baseV, float hitboxMultiplier, Dimension baseDim, int ord, int num) {

		species = s;
		instance = iName;
		ordinal = ord;
		this.baseDim = baseDim;
		this.baseV = baseV;
		this.hitboxMultiplier = hitboxMultiplier;
		this.numberOfBufferedImages = num;
	}

	public int getOrdinal () {

		return ordinal;
	}
	
	public int getNumberOfBufferedImages () {
		
		return numberOfBufferedImages;
	}

	public String getObjectName () {

		return species;
	}

	public Class<?> getInstance () {

		return instance;
	}
	
	public int getBaseV() {
		return baseV;
	}

	public float getHitboxMultiplier() {
		return hitboxMultiplier;
	}

	public Dimension getBaseDim() {
		return baseDim;
	}

}
