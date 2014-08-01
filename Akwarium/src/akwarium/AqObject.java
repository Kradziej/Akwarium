package akwarium;

import java.awt.image.BufferedImage;

public abstract class AqObject {
	
	protected int x;
	protected int y;
	protected float[] vector = {0,0};     //put direction here
	//protected int direction = 1;
	protected int v;   // pixels per sec
	protected int imageWidth;
	protected int imageHeight;
	protected BufferedImage image;
	protected BufferedImage leftDirImage;
	protected BufferedImage rightDirImage;
}
