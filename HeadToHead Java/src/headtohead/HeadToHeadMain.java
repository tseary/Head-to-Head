package headtohead;

import javax.swing.JFrame;

import blasteroids.BlasteroidsGamePanel;

public class HeadToHeadMain {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Head to Head v0.31");
		
		// HeadToHeadGamePanel gamePanel = new PongGamePanel();
		HeadToHeadGameCanvas gameCanvas = new BlasteroidsGamePanel();
		frame.add(gameCanvas);
		
		frame.addKeyListener(gameCanvas);
		
		boolean windowed = true;
		
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
