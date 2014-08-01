package akwarium;

import java.awt.image.BufferedImage;

public class Mine extends Obstacles {

	private float[] vector = {-0.5f,0};
	private static BufferedImage image;
	private static int baseWidth = 45;
	private static int imageWidth;
	private static int imageHeight;
	private static int hitboxW;
	private static int hitboxH;

	private Mine () {

		v = (int)(3 * DrawAq.xAnimalScale());
	}


	Mine (Aquarium aq) {

		this();
		this.aq = aq;
		this.setInitialCoordinates();
		v = Math.round(v * aq.boost());
	}

	// Constructor for multiplayer
	Mine (Aquarium aq, int v) {

		this.aq = aq;
		this.v = v;
	}

	Mine (String name, int x, int y, Aquarium aq) {

		this();
		this.x = x;
		this.x = y;
		this.aq = aq;
	}

	Mine (int x, int y, Aquarium aq) {

		this();
		this.x = x;
		this.y = y;
		this.aq = aq;
	}

	public static void initResources() {

		image = scaleImage(mine, Math.round(baseWidth * DrawAq.xAnimalScale()));
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		hitboxW = imageWidth - Math.round(0.4f * imageWidth);
		hitboxH = imageHeight - Math.round(0.4f * imageHeight);
	}


	@Override
	public void run() {

		while(threadRun) {


			if(aq.isClient()) {

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


			int newX;
			int newY;

			// calculate new position from velocity and vector
			// check if it's out of window
			// constant movement for mines
			newX = Math.round((vector[0] * v) + x);
			newY = Math.round((vector[1] * v) + y);


			if(newY > aq.getAquariumHeight() - distanceFromBorderBottom)
				newY = aq.getAquariumHeight() - distanceFromBorderBottom;

			if(distanceFromBorderTop > newY)
				newY = distanceFromBorderTop;


			x = newX;
			y = newY;


			// Conditions when on shark -> terminated
			if(aq.containsShark(this)) {
				terminate();
				break;
			}

			// When out of the screen on left
			if(x < (0 - image.getWidth() - 40)) {
				terminate();
				break;
			}

			if (aq.isServer()) {
				PacketSender.sendNewCoordinates(index, x, y, direction);
			}


			sleepThread(SYNCH_TIME);
		}
	}

	@Override
	public String getSpeciesName () {

		return name;
	}

	@Override
	public int getSpeciesCode () {

		return CODE;
	}

	@Override
	public int getImageWidth () {

		return imageWidth;
	}

	@Override
	public int getImageHeight () {

		return imageHeight;
	}

	@Override
	public int getHitboxW () {

		return hitboxW;
	}

	@Override
	public int getHitboxH () {

		return hitboxH;
	}

	@Override
	protected BufferedImage getImage () {

		return image;
	}

	@Override
	protected void setImageIndex(int imageIndex) {}
}
