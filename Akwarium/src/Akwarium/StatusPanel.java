package Akwarium;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class StatusPanel extends JPanel {
	
	Aquarium aq;
	double healthOwnerM = 1.0d;
	double healthPlayerM = 1.0d;
	int healthbarWidth;
	int healthbarHeight;
	int width;
	int height;
	float stringHeight;
	int ownerStringPosX;
	int playerStringPosX;
	int playerHealthPosX;
	String pointsOwner = "0";
	String pointsPlayer = "0";
	FontMetrics metrics;
	Font font;
	boolean isMultiplayer;
	
	public StatusPanel(boolean isMultiplayer) {
		
		this.aq = aq;
		font = new Font("Consolas", Font.PLAIN, (int)(DrawAq.getResolution().getHeight() * 0.045d));
		healthbarWidth = (int)(160 * DrawAq.xAnimalScale());
		healthbarHeight = (int)(24 * DrawAq.yAnimalScale());
		stringHeight = (float)(21 * DrawAq.yAnimalScale());
		ownerStringPosX = 35 + healthbarWidth;
		this.isMultiplayer = isMultiplayer;
	}
	
	
	public void paint(Graphics g) {
		
		super.paint(g);
		Graphics2D g2d = (Graphics2D)g;
		if (metrics == null) {
			metrics = g2d.getFontMetrics(font);
			width = this.getWidth();
			height = this.getHeight();
			playerHealthPosX = width - healthbarWidth - 30;
			
		}
		
		g2d.setColor(new Color(255, 0, 0));
		g2d.fillRect(30, 0, (int)(healthOwnerM * healthbarWidth), height);
		
		if(isMultiplayer) {
			int hToSub = (int)((1 - healthPlayerM) * healthbarWidth);
			g2d.fillRect(playerHealthPosX + hToSub, 0, healthbarWidth - hToSub, height);
		}
		g2d.setColor(new Color(0, 0, 0));
		g2d.setFont(font);
		g2d.drawString(pointsOwner, ownerStringPosX, stringHeight);
		if(isMultiplayer) {
			playerStringPosX = width - healthbarWidth - 35 - metrics.stringWidth(pointsPlayer);
			g2d.drawString(pointsPlayer, playerStringPosX, stringHeight);
		}
	}
	
	public void setHealthMultiplierOwner (double m) {
		
		this.healthOwnerM = m;
		repaint();
	}
	
	public void setHealthMultiplierPlayer (double m) {
		
		this.healthPlayerM = m;
		repaint();
	}
	
	public void setPointsOwner (int p) {
		
		pointsOwner = ""+p;
		repaint();
	}
	
	public void setPointsPlayer (int p) {
		
		pointsPlayer = ""+p;
		repaint();
	}

}
