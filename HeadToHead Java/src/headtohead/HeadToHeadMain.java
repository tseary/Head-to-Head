package headtohead;

import java.io.File;

import javax.swing.JFrame;

import blasteroids.BlasteroidsGamePanel;

public class HeadToHeadMain {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Head to Head v0.31");
		
		// HeadToHeadGamePanel gamePanel = new PongGamePanel();
		HeadToHeadGameCanvas gameCanvas = new BlasteroidsGamePanel();
		frame.add(gameCanvas);
		
		frame.addKeyListener(gameCanvas);
		
		File debugFile = new File("debug.txt");
		boolean windowed = debugFile.exists();
		
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
