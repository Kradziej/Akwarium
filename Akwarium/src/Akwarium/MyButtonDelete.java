package Akwarium;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.JButton;

public class MyButtonDelete extends JButton implements ActionListener {

	private Aquarium aquarium;
	private String name;
	
	MyButtonDelete(Aquarium aquarium, String name) {
		
		super(name);
		this.name = name;
		this.aquarium = aquarium;
		this.addActionListener(this);
	}
	
	public synchronized void actionPerformed(ActionEvent e) {
		
		if( !((JButton)e.getSource()).isEnabled() )
			return;
		
		((JButton)e.getSource()).setEnabled(false);
		int allAnimals = aquarium.getNumberOfAnimals();
		if(allAnimals == 0)
			return;
		Random rand = new Random();
		int n = rand.nextInt(allAnimals);
		//aquarium.removeAnimal(n);
		((JButton)e.getSource()).setEnabled(true);
	}
	
	public synchronized void setEnabled(boolean b) {
		
		synchronized(this) {
			super.setEnabled(b);
		}
	}
	
}
