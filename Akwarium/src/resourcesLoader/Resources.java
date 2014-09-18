package resourcesLoader;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;



import java.util.Random;

import javax.imageio.ImageIO;

import packet.PacketSender;
import akwarium.Animal;
import akwarium.AqObject;
import akwarium.AqObjectsEnum;
import akwarium.Aquarium;
import akwarium.DrawAq;
import akwarium.Program;
import akwarium.SpeciesList;

public class Resources {
	
	ImageTools conv;
	//private BufferedImage[] resources;
	private HashMap<String, BufferedImage> resources;
	// 1dim = all species resource list // 2dim 0-left image 1-right image // 3dim different colors
	private BufferedImage[][][] graphics;
	private BufferedImage blank;
	private static Resources res;
	private boolean loaded;
	
	private Resources() {
		
		conv = new ImageTools();
		resources = new HashMap<>();
	}
	
	public static Resources getInstance() {
		
		if(res == null)
			res = new Resources();
		
		return res;
	}
	
	public BufferedImage getGraphics(int ordinal, int orientation, int index) {
		
		return graphics[ordinal][orientation][index];
	}
	
	public BufferedImage getBlank() {
		
		return blank;
	}
	
	public BufferedImage getResourceByName(String name) {
		
		return resources.get(name);
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
			
			int numImages = aObj.getNumberOfBufferedImages();
			
			graphics[aObj.getOrdinal()] = new BufferedImage[2][];
			graphics[aObj.getOrdinal()][0] = new BufferedImage[numImages];
			graphics[aObj.getOrdinal()][1] = new BufferedImage[numImages];
			
			for(int i = 0; i < numImages; i++) {
			
				BufferedImage rImg = conv.copyImage( resources.get(aObj.getObjectName()) );
				int width;
				Color c;
				if(numImages != 1) {
					width = aObj.getBaseDim().width + rand.nextInt((int)(width * 0.4f));
					c = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
					conv.changeImageColor(rImg, maskColor, c);
					rImg = conv.scaleImage(rImg, Math.round(width * DrawAq.xAnimalScale()));
				}
				
				BufferedImage lImg = conv.flipImage(conv.copyImage(rImg));

				graphics[aObj.getOrdinal()][0][i] = lImg;
				graphics[aObj.getOrdinal()][1][i] = rImg;
				
				if(aq.isMultiplayer())
					PacketSender.initializeImages(aObj.getOrdinal(), i, width, c);
			}

		}
		
		loaded = true;
		PacketSender.imagesInitializationEnd();
	}
	
	public boolean initAnimalsClient (int ordinal, int index, Color color, int width, boolean loaded) {

		// SEND INIT TO CLIENT XD
		Color maskColor = new Color(255,255,255);
		AqObjectsEnum[] objects = AqObjectsEnum.values();
		AqObjectsEnum aObj = objects[ordinal];
		int numImages = aObj.getNumberOfBufferedImages();
		
		if(graphics == null) {
			int speciesNumber = AqObjectsEnum.values().length;
			graphics = new BufferedImage[speciesNumber][][];
		}
		
		if(graphics[ordinal] == null)
			graphics[ordinal] = new BufferedImage[2][];
		if(graphics[ordinal][0] == null) {
			graphics[ordinal][0] = new BufferedImage[numImages];
			graphics[ordinal][1] = new BufferedImage[numImages];
		}

		BufferedImage rImg = conv.copyImage(resources.get(aObj.getObjectName()));
		if(numImages != 1) {
			conv.changeImageColor(rImg, maskColor, color);
			rImg = conv.scaleImage(rImg, Math.round(width * DrawAq.xAnimalScale()));
		}
		BufferedImage lImg = conv.flipImage(conv.copyImage(rImg));
		graphics[ordinal][0][index] = lImg;
		graphics[ordinal][1][index] = rImg;

		if(loaded) {
			this.loaded = loaded;
			return true;		// if all images are loaded
		}
			
		return loaded;
	}
	

}
