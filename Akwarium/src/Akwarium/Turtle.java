package Akwarium;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.imageio.ImageIO;

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
	
	
	public String getSpeciesName () {
		
		return species.getSpeciesName();
	}
	
	public int getSpeciesCode () {
		
		return species.getOrdinal();
	}

	
	public void setImageIndex(int imageIndex) {

		int index = species.getOrdinal();
		leftDirImage = graphics[index][0][imageIndex];
		rightDirImage = graphics[index][1][imageIndex];
		image = rightDirImage;
	}
}
