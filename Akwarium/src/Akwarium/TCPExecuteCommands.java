package Akwarium;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;

public class TCPExecuteCommands extends Thread {

	private int x;
	private int y;
	private int v;
	private String name;
	private int imageIndex;
	private Aquarium Aq;
	private int code;
	
	
	TCPExecuteCommands(Aquarium Aq, int code, int imageIndex, int x, int y, int v, String name) {
		
		this.code = code;
		this.Aq = Aq;
		this.x = x;
		this.y = y;
		this.v = v;
		this.name = name;
		this.imageIndex = imageIndex;
		start();
	}
	
	public void run() {
	
		/*
		try {
			Aq.addAnimal(code, imageIndex, x, y, v, name);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			System.out.println("Cannot create pet");
			e.printStackTrace();
			System.exit(-1);
		}*/
	}
}
