package akwarium;

import java.util.Random;

public class Obstacles extends AqObject {
	
	protected static int distanceFromBorderLeft = 0;
	protected static int distanceFromBorderRight = 135;
	protected static int distanceFromBorderTop = 15;
	protected static int distanceFromBorderBottom = 60;
	protected static final int NUMBER_OF_BUFFERED_IMAGES = 1;
	
	
	
	protected void setInitialCoordinates () {

		Random rand = new Random();
		this.x = aq.getAquariumWidth() + 60;
		this.y = rand.nextInt(aq.getAquariumHeight() - 20) + 10;
	}
}
