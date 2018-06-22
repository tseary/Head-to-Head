package headtohead;

import javax.swing.JFrame;

import blasteroids.BlasteroidsGamePanel;

public class HeadToHeadMain {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Head to Head v0.31");
		
		// HeadToHeadGamePanel gamePanel = new PongGamePanel();
		HeadToHeadGamePanel gamePanel = new BlasteroidsGamePanel();
		frame.setContentPane(gamePanel);
		
		frame.addKeyListener(gamePanel);
		
		boolean windowed = false;
		
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
		
		gamePanel.newGame();
	}
}
