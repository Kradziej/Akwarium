package Akwarium;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatusPanel extends JPanel {
	
	Aquarium aq;
	double healthOwnerM = 1.0d;
	double healthPlayerM = 1.0d;
	int healthbarWidth;
	int healthbarHeight;
	String pointsOwner = "10001";
	String pointsPlayer = "100001";
	FontMetrics metrics;
	Font font;
	
	public StatusPanel(Aquarium aq) {
		
		this.aq = aq;
		font = new Font("Consolas", Font.PLAIN, (int)(DrawAq.getResolution().getHeight() * 0.045d));
		healthbarWidth = (int)(160 * DrawAq.xAnimalScale());
		healthbarHeight = (int)(24 * DrawAq.yAnimalScale());
	}
	
	
	public void paint(Graphics g) {
		
		Graphics2D g2d = (Graphics2D)g;
		if (metrics == null)
			metrics = g2d.getFontMetrics(font);
		
		int width = this.getWidth();
		int height = this.getWidth();
		g2d.setColor(new Color(255, 0, 0));
		g2d.fillRect(30, 0, (int)(healthOwnerM * healthbarWidth), height);
		int hToSub = (int)((1 - healthPlayerM) * healthbarWidth);
		g2d.fillRect((width - healthbarWidth - 30) + hToSub, 0, healthbarWidth - hToSub, height);
		g2d.setColor(new Color(0, 0, 0));
		g2d.setFont(font);
		g2d.drawString(pointsOwner, 35 + healthbarWidth, 21 * DrawAq.yAnimalScale());
		g2d.drawString(pointsPlayer, width - healthbarWidth - 35 - metrics.stringWidth(pointsPlayer), 21 * DrawAq.yAnimalScale());
		System.out.println(metrics.stringWidth(pointsPlayer));
	}
	
	public void setHealthMultiOwner (double m) {
		
		this.healthOwnerM = m;
	}
	
	public void setHealthMultiPlayer (double m) {
		
		this.healthPlayerM = m;
	}

}
