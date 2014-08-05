package resourcesLoader;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Random;

import akwarium.Animal;
import akwarium.AqObjectsEnum;
import akwarium.Aquarium;
import akwarium.DrawAq;
import akwarium.PacketSender;

class ImageTools {
	
	
	BufferedImage flipImage (BufferedImage img) {

		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-img.getWidth(null), 0);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		return op.filter(img, null);
	}
	
	
	BufferedImage scaleImage (BufferedImage img, int width) {

		if(width == 0)
			return img;
		
		int height = (int)(width * ((double)img.getHeight() / img.getWidth()));
		BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = buff.createGraphics();
		g.drawImage(img.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
		g.dispose();
		return buff;
	}
	
	BufferedImage copyImage (BufferedImage src) {

		BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = copy.createGraphics();
		g.drawImage(src, 0, 0, null);
		g.dispose();
		return copy;
	}
	
	void changeImageColor (BufferedImage image, Color maskColor, Color c) {


		if(maskColor.equals(c))
			return;

		for(int i = 0; i < image.getWidth(null); i++) {
			for(int j = 0; j < image.getHeight(null); j++) {

				int rgb = image.getRGB(i,j);
				if (((rgb >> 24) & 0xFF) == 0)
					continue;

				rgb = rgb | (0xFF << 24);
				if( rgb == maskColor.getRGB() )
					try {
						image.setRGB(i, j, c.getRGB());
					} catch(NullPointerException e) {
						e.printStackTrace();
						System.exit(0);
					}
			}
		}
	}

}
