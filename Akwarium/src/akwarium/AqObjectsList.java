package akwarium;

public enum AqObjectsList {
	
	FISH("fish", Fish.class, 0x0), 
	TURTLE("turtle", Turtle.class, 0x1),
	MINE("naval_mine", Mine.class, 0x2),
	OWNER("shark_blue", Shark.class, 0xFE),
	PLAYER("shark_red", Shark.class, 0xFF);
	
	private String species;
	private Class<? extends Animal> instance;
	private int ordinal;

	AqObjectsList (String s, Class<? extends Animal> iName, int ord) {

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
