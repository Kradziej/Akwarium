package akwarium;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

public class MyButtonAdd extends JButton implements ActionListener {

	private Aquarium aquarium;
	private String name;
	private static Color color;

	MyButtonAdd(Aquarium aquarium, String name) {

		super(name);
		this.name = name;
		this.aquarium = aquarium;
		this.addActionListener(this);
	}


	@Override
	public synchronized void actionPerformed(ActionEvent e) {

		if( !((JButton)e.getSource()).isEnabled() )
			return;
		/*
		((JButton)e.getSource()).setEnabled(false);
		try {
			if(color != null)
				aquarium.addAnimal(color);
			else
				aquarium.addAnimal();
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e1) {
				System.out.println("Cannot create new pet");
			}
		((JButton)e.getSource()).setEnabled(true);
		 */
	}

	@Override
	public synchronized void setEnabled(boolean b) {

		super.setEnabled(b);
	}

	public static void setColor(Color c) {

		color = c;
	}

}
