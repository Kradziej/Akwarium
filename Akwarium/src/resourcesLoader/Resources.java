package resourcesLoader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;



import javax.imageio.ImageIO;

import akwarium.Program;

public class Resources {
	
	ImageTools conv;
	//private BufferedImage[] resources;
	private HashMap<String, BufferedImage> resources;
	// 1dim = all species resource list // 2dim 0-left image 1-right image // 3dim different colors
	private BufferedImage[][][] graphics;
	private BufferedImage blank;
	
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
	

}
