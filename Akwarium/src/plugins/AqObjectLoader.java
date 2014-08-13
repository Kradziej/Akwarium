package plugins;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import akwarium.Program;

public class AqObjectLoader {
	
	private String pluginFolderName = "plugins";
	
	// located in new directory where jar file is located 
	public void getJavaFiles () {
		
		URL jarLocationURL = Program.class.getProtectionDomain().getCodeSource().getLocation();
		File jarFile = null;
		
		try {
			jarFile = new File(jarLocationURL.toURI());
		} catch (URISyntaxException e) {
			System.out.println("Incorrect path to plugins: " + jarLocationURL.toString());
		}
		
		if(jarFile.isFile()) {
			
			jarFile.getParentFile().listFiles();
		}
	}

}
