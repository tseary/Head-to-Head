package headtohead;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;

public class HeadToHeadMain {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Head to Head v0.36");
		
		GameSelectionCanvas selectionCanvas = new GameSelectionCanvas();
		
		// Add game thumbnails
		Random random = new Random(0);
		for (int i = 0; i < 3; i++) {
			BufferedImage thumb = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
			Graphics g = thumb.createGraphics();
			g.setColor(new Color(random.nextInt()));
			g.fillOval(0, 0, 99, 99);
			g.drawOval(0, 0, 99, 99);
			selectionCanvas.addThumbnail(thumb, new Color(random.nextInt()));
		}
		
		// HeadToHeadGameCanvas gameCanvas = new PongGameCanvas();
		// HeadToHeadGameCanvas gameCanvas = new BlasteroidsGameCanvas();
		HeadToHeadGameCanvas gameCanvas = selectionCanvas;
		frame.add(gameCanvas);
		
		frame.addKeyListener(gameCanvas);
		
		boolean windowed = DebugMode.isEnabled();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if (windowed) {
			// frame.setLocationByPlatform(true);
			frame.setResizable(true);
			frame.pack();
		} else {
			frame.setUndecorated(true);
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}
		frame.setVisible(true);
		
		// Double-buffer the graphics
		gameCanvas.createBufferStrategy(2);
		
		gameCanvas.newGame();
	}
}
