package headtohead;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import blasteroids.BlasteroidsGameCanvas;
import pong.PongGameCanvas;
import tankbattle.TankBattleGameCanvas;

public class HeadToHeadMain {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Head to Head v0.36");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		HeadToHeadGameCanvas gameCanvas;
		if (!DebugMode.isEnabled()) {
			GameSelectionCanvas gameSelector = createGameSelectionCanvas();
			
			addGameCanvas(frame, gameSelector);
			initializeFrame(frame);
			doubleBuffer(gameSelector);
			
			gameSelector.newGame();
			
			// Wait for the user to select a game
			try {
				gameSelector.startGameLoop(false);
				gameSelector.joinGameLoopThread();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			removeGameCanvas(frame, gameSelector);
			
			// Create an instance of the selected game
			switch (gameSelector.getSelectedIndex()) {
				case 0:	// Pong
					gameCanvas = new PongGameCanvas();
					break;
				default:
				case 1:	// Blasteroids
					gameCanvas = new BlasteroidsGameCanvas();
					break;
				case 2:	// Tank Battle
					gameCanvas = new TankBattleGameCanvas();
					break;
			}
			gameSelector = null;
		} else {
			gameCanvas = new TankBattleGameCanvas();
		}
		
		// Set up the selected game
		addGameCanvas(frame, gameCanvas);
		initializeFrame(frame);
		doubleBuffer(gameCanvas);
		
		// Run the game forever
		try {
			do {
				gameCanvas.newGame();
				gameCanvas.startGameLoop();
				gameCanvas.joinGameLoopThread();
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
		
		gameSelector.addThumbnail(loadImage("/thumbs/pong.png"), Color.YELLOW);
		gameSelector.addThumbnail(loadImage("/thumbs/blasteroids.png"), Color.DARK_GRAY);
		gameSelector.addThumbnail(loadImage("/thumbs/tankbattle.png"), Color.RED);
		
		return gameSelector;
	}
	
	private static Image loadImage(String path) {
		try {
			return ImageIO.read(HeadToHeadMain.class.getResource(path));
		} catch (Exception e) {
			final int imageSize = 100;
			BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
			Graphics g = image.createGraphics();
			g.setColor(Color.RED);
			g.drawLine(0, 0, imageSize - 1, imageSize - 1);
			g.drawLine(0, imageSize - 1, imageSize - 1, 0);
			return image;
		}
	}
	
	private static void initializeFrame(JFrame frame) {
		boolean windowed = DebugMode.isEnabled();
		
		if (windowed) {
			// frame.setLocationByPlatform(true);
			frame.setResizable(true);
			frame.pack();
		} else {
			if (!frame.isDisplayable()) {
				frame.setUndecorated(true);
			}
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
