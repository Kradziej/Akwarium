package Akwarium;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Shape;

public class DrawAq extends Canvas {

	private Aquarium Aq;
	//public static int width = 1152;
	//public static int height = 768;
	private static int resolutionIndex;
	private static Dimension[] resolutions = {new Dimension(800, 600), new Dimension(1024, 768), 
			new Dimension(1152, 864), new Dimension(1280, 800), new Dimension(1280, 960), 
			new Dimension(1444, 900), new Dimension(1680, 1050), new Dimension(1920, 1200)};
	private Image buffer;
	private Graphics2D g2dBuffer;
	private static float xScale;
	private static float yScale;
	private static float animalScale;
	//private BufferedImage background; 
	
	DrawAq (Aquarium Aq) {
	
		this.Aq = Aq;
		/*try { 
			background = ImageIO.read(new File("src/resources/background.jpg"));
		} catch (IOException e) {
			System.out.println("Cannot load resources!");
			e.printStackTrace();
			System.exit(-1);
		}*/
	}
	
	public static JFrame createFrame (String name) {
		
		JFrame frame = new JFrame(name);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(resolutions[resolutionIndex]);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new BorderLayout());
		frame.setResizable(false);
		// set animal scale
		animalScale = (float)(resolutions[resolutionIndex].getWidth() / 800);
		return frame;
	}
	
	public static JPanel createPanel (LayoutManager Layout) {
		
		JPanel panel = new JPanel(Layout); 
		return panel;
	}
	
	public void clear () {
		
		Graphics g = this.getGraphics();
		Graphics2D g2d = (Graphics2D)g;
		g2d.clearRect(0, 0, Aq.getAquariumWidth(), Aq.getAquariumHeight());
	}
	
	public void clearShape (int x, int y, Shape s) {
		
		Graphics g = this.getGraphics();
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(g2d.getBackground());
		g2d.translate(x, y);
		g2d.fill(s);
	}


	public void drawAnimal (Animal a) {
		
		g2dBuffer.drawImage(a.getImage(), a.getX(), a.getY(), this);
	}
	
	public void paint (Graphics g) {
		
		super.paint(g);
	}
	
	public void createBuffer () {
		
		buffer = createImage(Aq.getAquariumWidth(), Aq.getAquariumHeight());
		g2dBuffer = (Graphics2D)buffer.getGraphics();
	}
	
	public void drawBuffer () {
		
		Graphics g = this.getGraphics();
		Graphics2D g2d = (Graphics2D)g;
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    g2d.setRenderingHints(rh);
		g2d.drawImage(buffer, 0, 0, null);
		g2dBuffer.dispose();
	}
	
	public static String[] getAvailableResolutions () {
		
		String[] res = new String[resolutions.length];
		for (int i = 0; i < res.length; i++) {
			
			res[i] = (int)resolutions[i].getWidth() + "x" + (int)resolutions[i].getHeight();
		}
	
		return res;
	}
	
	public static void setResolution (int index) {
		
		resolutionIndex = index;
	}
	
	public static Dimension getResolution () {
		
		return resolutions[resolutionIndex];
	}
	
	public static void setScales (float x, float y) {
		
		xScale = x;
		yScale = y;
	}
	
	public static float xScale () {
		
		return xScale;
	}
	
	public static float yScale () {
		
		return yScale;
	}
	
	public static float animalScale () {
		
		return animalScale;
	}
}
