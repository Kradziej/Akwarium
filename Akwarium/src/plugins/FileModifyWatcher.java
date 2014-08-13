package plugins;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileModifyWatcher implements Runnable {
	
	private WatchService watcher;
	private Path path;
	private Thread t;
	
	public FileModifyWatcher(Path path) {
		
		this.path = path;
		try {
			watcher = FileSystems.getDefault().newWatchService();
			path.register(watcher, ENTRY_MODIFY);
		} catch (IOException e) {
			System.out.println("Cannot create watcher " + e.getClass());
			e.printStackTrace();
		}
		
		t = new Thread(this);
		t.start();
	}


	@Override
	public void run() {
	
		while(true) {
			
			WatchKey key = null;
			
			try {
				key = watcher.take();
			} catch (InterruptedException e) {
				System.out.println("Watcher Interrupted!");
			}
			
			for(WatchEvent<?> watcherEvent :  key.pollEvents()) {
				
				WatchEvent<Path> pathEvent = (WatchEvent<Path>)watcherEvent;
				
				if(watcherEvent.kind() == OVERFLOW)
					continue;
				
				Path p = pathEvent.context();
				System.out.println("File " + p.toString() + " modified!");
				

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			if (!key.reset())
				break;
	       
		}
		
	}

}
