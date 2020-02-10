package headtohead;

import java.awt.Canvas;
import java.awt.Color;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import blasteroids.BlasteroidsGameCanvas;
import pong.PongGameCanvas;

public class HeadToHeadMain {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Head to Head v0.36");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		GameSelectionCanvas gameSelector = createGameSelectionCanvas();
		
		addGameCanvas(frame, gameSelector);
		initializeFrame(frame);
		doubleBuffer(gameSelector);
		
		gameSelector.newGame();
		
		// Wait for the user to select a game
		try {
			gameSelector.startGameLoop();
			gameSelector.joinGameLoopThread();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		removeGameCanvas(frame, gameSelector);
		
		// Create an instance of the selected game
		HeadToHeadGameCanvas gameCanvas;
		switch (gameSelector.getSelectedIndex()) {
			case 0:	// Pong
				gameCanvas = new PongGameCanvas();
				break;
			default:
			case 1:	// Blasteroids
				gameCanvas = new BlasteroidsGameCanvas();
				break;
			/*case 2:	// Tank Battle
				gameCanvas = new TankBattleGameCanvas();
				break;*/
		}
		gameSelector = null;
		
		// Set up the selected game
		addGameCanvas(frame, gameCanvas);
		initializeFrame(frame);
		doubleBuffer(gameCanvas);
		
		// Run the game forever
		try {
			do {
				System.out.println("Starting game.");
				gameCanvas.newGame();
				gameCanvas.startGameLoop();
				gameCanvas.joinGameLoopThread();
				System.out.println("Game finished.");
				System.gc();	// Run the garbage collector
			} while (true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// TODO Show continue/return to menu screen
		
		System.exit(0);
	}
	
	private static GameSelectionCanvas createGameSelectionCanvas() {
		GameSelectionCanvas gameSelector = new GameSelectionCanvas();
		
		try {
			gameSelector.addThumbnail(
					ImageIO.read(HeadToHeadMain.class.getResource("/thumbs/pong.png")), Color.YELLOW);
			gameSelector.addThumbnail(
					ImageIO.read(HeadToHeadMain.class.getResource("/thumbs/blasteroids.png")), Color.DARK_GRAY);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return gameSelector;
	}
	
	private static void initializeFrame(JFrame frame) {
		boolean windowed = DebugMode.isEnabled();
		
		if (windowed) {
			// frame.setLocationByPlatform(true);
			frame.setResizable(true);
			frame.pack();
		} else {
			frame.setUndecorated(true);
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}
		frame.setVisible(true);
	}
	
	private static void addGameCanvas(JFrame frame, HeadToHeadGameCanvas gameCanvas) {
		frame.add(gameCanvas);
		frame.addKeyListener(gameCanvas);
	}
	
	private static void removeGameCanvas(JFrame frame, HeadToHeadGameCanvas gameCanvas) {
		frame.remove(gameCanvas);
		frame.removeKeyListener(gameCanvas);
	}
	
	private static void doubleBuffer(Canvas canvas) {
		canvas.createBufferStrategy(2);
	}
}
