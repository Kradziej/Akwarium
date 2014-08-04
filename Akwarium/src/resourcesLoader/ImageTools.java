package resourcesLoader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Random;
import akwarium.Animal;
import akwarium.AqObjectsEnum;
import akwarium.Aquarium;
import akwarium.DrawAq;
import akwarium.PacketSender;

class ImageTools {
	
	
	BufferedImage scaleImage (BufferedImage img, int width, int height) {

		//int height = (int)(width * ((double)img.getHeight()/img.getWidth()));
		BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = buff.createGraphics();
		g.drawImage(img.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
		g.dispose();
		return buff;
	}

}
