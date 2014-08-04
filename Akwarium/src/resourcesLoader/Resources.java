package resourcesLoader;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;



import java.util.Random;

import javax.imageio.ImageIO;

import akwarium.Animal;
import akwarium.AqObjectsEnum;
import akwarium.Aquarium;
import akwarium.DrawAq;
import akwarium.PacketSender;
import akwarium.Program;

public class Resources {
	
	ImageTools conv;
	//private BufferedImage[] resources;
	private static HashMap<String, BufferedImage> resources;
	// 1dim = all species resource list // 2dim 0-left image 1-right image // 3dim different colors
	private static BufferedImage[][][] graphics;
	private static BufferedImage blank;
	public static final int NUMBER_OF_BUFFERED_IMAGES = 30;
	
	public Resources() {
		
		conv = new ImageTools();
		resources = new HashMap<>();
	}
	
	
	public void loadResources () {

		try {
			
			File[] files = null;
			Enumeration<URL> foldersURL = null;
			URL folderURL;
			
			foldersURL = Program.class.getClassLoader().getResources("resources");
			if(foldersURL.hasMoreElements()) {
				folderURL = foldersURL.nextElement();
				File folder = new File(folderURL.toURI());
				files = folder.listFiles();
			} else {
				System.out.println("Failed to load resources");
				System.exit(-1);
			}
			
			
			for (int i = 0; i < files.length; i++) {
				
				String fileName = files[i].getName();
				fileName = fileName.substring(0, fileName.indexOf('.'));
				resources.put(fileName, ImageIO.read(files[i]));
			}
			
			blank = resources.get("blank");
				
		} catch (IOException | URISyntaxException e) {
			System.out.println("Cannot load resources");
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	
	public void initResources () {


		Color maskColor = new Color(255,255,255);
		Random rand = new Random();
		int speciesNumber = AqObjectsEnum.UNCONTROLLABLE.length;
		graphics = new BufferedImage[speciesNumber][][];
		
		for(AqObjectsEnum aObj : AqObjectsEnum.UNCONTROLLABLE) {
			
			graphics[aObj.getOrdinal()] = new BufferedImage[2][];
			graphics[aObj.getOrdinal()][0] = new BufferedImage[NUMBER_OF_BUFFERED_IMAGES];
			graphics[aObj.getOrdinal()][1] = new BufferedImage[NUMBER_OF_BUFFERED_IMAGES];
			
			for(int i = 0; i < NUMBER_OF_BUFFERED_IMAGES; i++) {
			
				BufferedImage rImg = copyImage( resources.get(aObj.getObjectName()) );
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
