package Akwarium;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;

public class myColorPicker extends JButton implements ActionListener, Runnable {

	private Aquarium aquarium;
	private String name;
	private Thread t;
	private boolean colorPickerVisible = true;
	
	myColorPicker(Aquarium aquarium, String name) {
		
		super(name);
		this.name = name;
		this.aquarium = aquarium;
		this.addActionListener(this);
	}
	
	
	public void run() {
		
		Color color = JColorChooser.showDialog(this, "Color Picker", null);
		myButtonAdd.setColor(color);
		colorPickerVisible = true;
	}
	
	
	public void actionPerformed(ActionEvent e) {
		
		if(colorPickerVisible) {
			
			colorPickerVisible = false;
			t = new Thread(this);
			t.start();
		}
	}

}
