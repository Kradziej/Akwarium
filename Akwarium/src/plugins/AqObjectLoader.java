package plugins;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import akwarium.Program;

public class AqObjectLoader {
	
	private String pluginFolderName = "plugins";
	
	// located in new directory where jar file is located 
	public Path getJavaFiles () {
		
		URL jarLocationURL = Program.class.getProtectionDomain().getCodeSource().getLocation();
		File jarFile = null;
		
		try {
			jarFile = new File(jarLocationURL.toURI());
		} catch (URISyntaxException e) {
			System.out.println("Incorrect path to plugins: " + jarLocationURL.toString());
		}
	
		Path pluginFolder =  Paths.get(jarFile.getParentFile().getAbsolutePath(), pluginFolderName);
		if(pluginFolder.toFile().isDirectory()) {
				
			System.out.println("Plugins folder doesn't exist!");
			return null;
		}
		
		return pluginFolder;
		
	}
	
	
	public void compilePlugins(Iterable<SimpleJavaFileObject> files) {
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
		
		CompilationTask task = compiler.getTask(null, null, diagnostics, null, null, files);
		boolean result = task.call();
		
		if(!result) {
			
			System.out.println("Plugins compilation failed");
			for(Diagnostic diag : diagnostics.getDiagnostics()) {
				
				System.out.println(diag.getKind().toString() + ": Line " + diag.getLineNumber());
				System.out.println(diag.getMessage(null));
			}
		}
		
	}
	
	public void loadPlugins() {}

}
