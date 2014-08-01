package resourcesLoader;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

import akwarium.Animal;
import akwarium.Aquarium;
import akwarium.DrawAq;
import akwarium.PacketSender;
import akwarium.Animal.SpeciesList;

public class ImageTools {
	
	
	public static void initResources (Aquarium aq) {


		Color maskColor = new Color(255,255,255);
		Random rand = new Random();
		int speciesNumber = SpeciesList.values().length;
		graphics = new BufferedImage[speciesNumber][][];

		for (int i = 0; i < speciesNumber; i++) {

			graphics[i] = new BufferedImage[2][];
			graphics[i][0] = new BufferedImage[NUMBER_OF_BUFFERED_IMAGES];
			graphics[i][1] = new BufferedImage[NUMBER_OF_BUFFERED_IMAGES];

			for(int j = 0; j < NUMBER_OF_BUFFERED_IMAGES; j++) {

				BufferedImage rImg = copyImage(Animal.resources[i]);
				int width = 50 + rand.nextInt(20);

				Color c = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
				changeImageColor(rImg, maskColor, c);
				rImg = scaleImage(rImg, Math.round(width * DrawAq.xAnimalScale()));
				BufferedImage lImg = flipImage(copyImage(rImg));

				graphics[i][0][j] = lImg;
				graphics[i][1][j] = rImg;
				if(aq.isMultiplayer())
					PacketSender.initializeImages(SpeciesList.values()[i].getOrdinal(), j, c, width);

			}
		}
	}

}
