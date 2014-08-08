package akwarium;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Random;

import packet.PacketSender;

public class Shark extends AqObject implements KeyListener {

	private HashSet<Integer> keys = new HashSet<Integer>();
	public static final int MAX_HEALTH = 100;
	private int health = MAX_HEALTH;
	private int points = 0;
	private boolean ishpLost;
	private boolean isOwner;
	private static int baseWidth = 160;
	private boolean specEffHealthDecrease;
	private int effectCounter = 0;
	private boolean effectActive;
	private AqObjectsEnum obj;
	private static final byte[] DECREASE_HEALTH = {1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,1};

	Shark() {}


	Shark(Aquarium aq, boolean isOwner) {

		this();
		this.aq = aq;
		this.isOwner = isOwner;

		if(isOwner) {
			rightDirImage = copyImage(sharkOwnerImage);
			obj = AqObjectsList.OWNER;
		} else {
			rightDirImage = copyImage(sharkPlayerImage);
			obj = AqObjectsList.PLAYER;
		}

		rightDirImage = scaleImage(rightDirImage, Math.round(baseWidth * DrawAq.xAnimalScale()) );
		leftDirImage = flipImage(copyImage(rightDirImage));
		image = rightDirImage;
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		hitboxW = imageWidth - Math.round(0.25f * imageWidth);
		hitboxH = imageHeight - Math.round(0.35f * imageHeight);
		this.setInitialCoordinates();
		v = (int)(19 * DrawAq.xAnimalScale());
	}
	
	@Override
	public void run() {


		while(threadRun) {

			int newX = 0;
			int newY = 0;

			// calculate new position from velocity and vector
			// check if it's out of window
			newX = Math.round((vector[0] * v) + x);
			newY = Math.round((vector[1] * v) + y);


			if(newX > aq.getAquariumWidth() - this.getImage().getWidth())
				newX = aq.getAquariumWidth() - this.getImage().getWidth();

			if(0 > newX)
				newX = 0;

			if(newY > aq.getAquariumHeight() - distanceFromBorderBottom)
				newY = aq.getAquariumHeight() - distanceFromBorderBottom;

			if(distanceFromBorderTop > newY)
				newY = distanceFromBorderTop;

			float boost = 1.0f;

			if(DrawAq.isMinimized())
				keys.clear();

			synchronized(keys) {
				for (int k : keys) {
					switch(k) {
					case KeyEvent.VK_UP:
						vector[1] = -boost;
						break;
					case KeyEvent.VK_DOWN:
						vector[1] = boost;
						break;
					case KeyEvent.VK_RIGHT:
						vector[0] = boost;
						break;
					case KeyEvent.VK_LEFT:
						vector[0] = -boost;
						break;
					}
				}
			}

			// flip image
			if(newX - x > 2)
				directionNew = 1;
			else if(newX - x < -2)
				directionNew = 0;

			direction = directionNew;


			if(effectActive) {

				byte[] EFFECT_PATTERN = null;

				if(specEffHealthDecrease) {

					EFFECT_PATTERN = DECREASE_HEALTH;
					ishpLost = true;
					switch(EFFECT_PATTERN[effectCounter]) {

					case 0:
						direction = 2;
						break;
					}
				}

				effectCounter++;
				if(effectCounter == EFFECT_PATTERN.length-1) {
					effectActive = false;
					specEffHealthDecrease = false;
					effectCounter = 0;
					ishpLost = false;
				}

			}

			x = newX;
			y = newY;

			imageChange(direction);

			if (aq.isMultiplayer()) {
				if(isOwner) {
					PacketSender.sharkUpdate(0, x, y, v, direction);
				} else {
					PacketSender.sharkUpdate(1, x, y, v, direction);
				}
			}

			sleepThread(SYNCH_TIME);
		}


	}


	@Override
	public String getSpeciesName () {

		return species;
	}

	@Override
	public int getSpeciesCode () {

		return CODE;
	}

	@Override
	public void keyPressed(KeyEvent e) {

		keys.add(e.getKeyCode());
	}


	@Override
	public void keyReleased(KeyEvent e) {

		int k =  e.getKeyCode();
		keys.remove(k);
		switch(k) {
		case KeyEvent.VK_UP:
			vector[1] = 0;
			break;
		case KeyEvent.VK_DOWN:
			vector[1] = 0;
			break;
		case KeyEvent.VK_RIGHT:
			vector[0] = 0;
			break;
		case KeyEvent.VK_LEFT:
			vector[0] = 0;
			break;
		}
	}


	@Override
	public void keyTyped(KeyEvent e) {}


	public void setHealth(int health) {

		this.health = health;
	}

	public int getHealth() {

		return health;
	}


	public void decreaseHealth(int minus) {

		health = health == 0 ? 0 : health - minus;
	}

	public void increaseHealth(int plus) {

		health = health + plus;
		if (health > 100)
			health = MAX_HEALTH;
	}

	public void setPoints (int points) {

		this.points = points;
	}

	public int getPoints () {

		return points;
	}


	public void addPoints(int bonus) {

		points = points + bonus;
	}

	@Override
	protected void setInitialCoordinates () {

		Random rand = new Random();
		this.x = 3;
		this.y = rand.nextInt(aq.getAquariumHeight() - 120) + 60;
	}


	public void specEffHealthDecrease () {

		if(effectActive)
			return;

		effectActive = true;
		specEffHealthDecrease = true;
	}

	public boolean ishpLost () {

		return ishpLost;
	}

	@Override
	protected void setImageIndex(int imageIndex) {}

}
