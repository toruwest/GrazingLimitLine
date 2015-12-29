package t.n.plainmap.view.dialog;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class MyPanel extends JPanel {
	private static final String IMAGE_FILE = "image/japanImage.jpg";
	private final BufferedImage orgImage;
	private BufferedImage resized;

	public MyPanel() throws IOException {
		super();
		setLayout(new BorderLayout());
    	orgImage = ImageIO.read(getClass().getClassLoader().getResource(IMAGE_FILE));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
//		if(resized == null) {
		//TODO リサイズされたときだけにしたい。
			resized = resize(orgImage, getWidth(), getHeight());
//		}
		((Graphics2D)g).drawImage(resized, 0, 0, null, null);
	}

	private static BufferedImage resize(BufferedImage image, int width, int height) {
	    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
	    Graphics2D g2d = bi.createGraphics();
	    g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
	    g2d.drawImage(image, 0, 0, width, height, null);
	    g2d.dispose();
	    return bi;
	}

}