package Akwarium;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Canvas;
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
	public static int width = 800;
	public static int height = 600;
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
		frame.setSize(width, height);
		frame.setLocationRelativeTo(null);
		frame.setLayout(new BorderLayout());
		frame.setResizable(false);
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


	public void drawAnimal (Graphics2D g2d, Animal a) {
		
		g2d.drawImage(a.getImage(), a.getX(), a.getY(), this);
	}
	
	public void paint (Graphics g) {
		
		super.paint(g);
	}
	
	public Image getBuffer () {
		
		Image buffer = createImage(Aq.getAquariumWidth(), Aq.getAquariumHeight());
		return buffer;
	}
	
	public void drawBuffer (Image buffer) {
		
		Graphics g = this.getGraphics();
		Graphics2D g2d = (Graphics2D)g;
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    g2d.setRenderingHints(rh);
		g2d.drawImage(buffer, 0, 0, null);
	}
	
	
	
	/*public void redrawAnimals () {
		
		Graphics g = this.getGraphics();
		Graphics2D g2d = (Graphics2D)g;
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    g2d.setRenderingHints(rh);
	    
	    Image buffer = createImage(Aq.getAquariumWidth(), Aq.getAquariumHeight());
		Graphics2D g2dBuffer = (Graphics2D)buffer.getGraphics();

		synchronized(Aq) {
			for(Animal a : Aq.getAnimals())
				drawAnimal(g2dBuffer, a);
		}
		
		g2d.drawImage(buffer, 0, 0, null);
		g2dBuffer.dispose();
	}*/
}
