package headtohead;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;

import blasteroids.BlasteroidsGameCanvas;

public class HeadToHeadMain {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Head to Head v0.36");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		GameSelectionCanvas selectionCanvas = createGameSelectionCanvas();
		addGameCanvas(frame, selectionCanvas);
		initializeFrame(frame);
		doubleBuffer(selectionCanvas);
		
		System.out.println("Starting game selection");
		selectionCanvas.newGame();
		
		//
		//
		//
		
		System.out.println("Waiting...");
		try {
			selectionCanvas.joinGameLoopThread();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Done waiting.");
		
		removeGameCanvas(frame, selectionCanvas);
		
		//
		//
		//
		
		// HeadToHeadGameCanvas gameCanvas = new PongGameCanvas();
		HeadToHeadGameCanvas gameCanvas = new BlasteroidsGameCanvas();
		addGameCanvas(frame, gameCanvas);
		initializeFrame(frame);
		doubleBuffer(gameCanvas);
		
		System.out.println("Starting game");
		gameCanvas.newGame();
		
		// TODO Show continue/return to menu screen
		
		/*try {
			gameCanvas.joinGameLoopThread();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.exit(0);*/
	}
	
	private static GameSelectionCanvas createGameSelectionCanvas() {
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
		
		return selectionCanvas;
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
