package akwarium;

import java.util.Random;

public class Turtle extends Animal {

	private SpeciesList species = SpeciesList.TURTLE;

	private Turtle () {

		Random rand = new Random();
		int index = species.getOrdinal();
		int selectImage = rand.nextInt(numberOfBufferedImages);
		leftDirImage = graphics[index][0][selectImage];
		rightDirImage = graphics[index][1][selectImage];
		imageIndex = selectImage;
		image = rightDirImage;
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		hitboxW = imageWidth - Math.round(0.3f * imageWidth);
		hitboxH = imageHeight - Math.round(0.3f * imageHeight);
		v = (int)(5 * DrawAq.xAnimalScale());
	}


	Turtle (Aquarium aq) {

		this();
		this.aq = aq;
		this.setInitialCoordinates();
		v = Math.round(v * aq.boost());
	}

	// Constructor for multiplayer
	Turtle (Aquarium aq, int v) {

		this.aq = aq;
		this.v = v;
	}

	Turtle (String name, int x, int y, Aquarium aq) {

		this();
		this.x = x;
		this.x = y;
		this.aq = aq;
	}

	Turtle (int x, int y, Aquarium aq) {

		this();
		this.x = x;
		this.y = y;
		this.aq = aq;
	}


	@Override
	public String getSpeciesName () {

		return species.getSpeciesName();
	}

	@Override
	public int getSpeciesCode () {

		return species.getOrdinal();
	}


	@Override
	public void setImageIndex(int imageIndex) {

		int index = species.getOrdinal();
		leftDirImage = graphics[index][0][imageIndex];
		rightDirImage = graphics[index][1][imageIndex];
		image = rightDirImage;
	}
}
