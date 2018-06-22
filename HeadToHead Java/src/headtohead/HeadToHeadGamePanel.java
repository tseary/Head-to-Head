package headtohead;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Timer;

import sound.SoundPlayer;

public abstract class HeadToHeadGamePanel extends JPanel
		implements ActionListener, KeyListener, MouseListener {
	private static final long serialVersionUID = 1L;
	
	protected ArcadeButton[] buttons;
	protected Player[] players;
	
	private final float aspectRatio = 4f / 3f;
	private int gameWidth, gameHeight;
	
	private int videoScale;
	
	protected BufferedImage videoFrame;
	
	/** The size of the output video on-screen in pixels. */
	protected int videoWidth, videoHeight;
	
	// Game loop
	private GameLoop gameLoopRunnable;
	private Thread gameLoopThread;
	
	// Demo mode
	private final int demoIdleTime = 60000; // Go to demo mode after 60s of inactivity
	protected Timer demoTimer;
	protected boolean demoMode = false;
	
	// Sound
	protected SoundPlayer sound;
	
	public HeadToHeadGamePanel(int newGameWidth, int gameTimerFPS) {
		// Calculate the size of the video
		gameWidth = newGameWidth;
		gameHeight = (int) (gameWidth * aspectRatio);
		initializeVideoScale();
		
		// Set up the panel
		setPreferredSize(new Dimension(videoWidth, videoHeight));
		setBackground(Color.BLACK);
		
		// Hide cursor
		BufferedImage cursorImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(
				cursorImage, new Point(), "blank cursor");
		this.setCursor(cursor);
		
		// Mouse listener
		this.addMouseListener(this);
		
		// Create the video frame
		videoFrame = new BufferedImage(gameWidth, gameHeight,
				BufferedImage.TYPE_INT_RGB);
		
		initializeButtons();
		initializePlayers();
		
		gameLoopRunnable = new GameLoop(this);
		
		demoTimer = new Timer(demoIdleTime, this);
		demoTimer.setActionCommand("DemoMode");
		
		sound = new SoundPlayer();
	}
	
	/** Calculates the best size for the video based on the screen size. */
	private void initializeVideoScale() {
		// Get the screen size
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		videoScale = Math.min((int) screenSize.getWidth() / gameWidth,
				(int) screenSize.getHeight() / gameHeight);
		videoScale = Math.max(videoScale, 1);
		
		// Set the video size
		videoWidth = videoScale * gameWidth;
		videoHeight = videoScale * gameHeight;
	}
	
	protected void initializeButtons() {
		buttons = new ArcadeButton[6];
		
		int[] buttonKeyCodes = new int[] {
				KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D,
				KeyEvent.VK_J, KeyEvent.VK_K, KeyEvent.VK_L };
		
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new ArcadeButton(buttonKeyCodes[i]);
		}
	}
	
	protected void initializePlayers() {
		players = new Player[2];
		
		ArcadeButton[] player0Buttons = new ArcadeButton[] {
				buttons[0], buttons[1], buttons[2] };
		ArcadeButton[] player1Buttons = new ArcadeButton[] {
				buttons[3], buttons[4], buttons[5] };
		
		players[0] = new Player(player0Buttons, new Color(0xff121e)); // Red
		players[1] = new Player(player1Buttons, new Color(0xf7e700)); // Yellow
	}
	
	public void startGameLoop() {
		stopGameLoop();
		
		// Start a new thread
		gameLoopThread = new Thread(gameLoopRunnable);
		gameLoopThread.start();
	}
	
	/**
	 * Blocks until the game loop thread dies.
	 */
	public void stopGameLoop() {
		// Stop the old thread if there is one
		if (gameLoopThread != null && gameLoopThread.isAlive()) {
			gameLoopThread.interrupt();
			gameLoopThread = null;
		}
	}
	
	protected void setPlayerHand(int playerIndex, boolean leftHanded) {
		// Create an array of buttons in the user's preferred order
		ArcadeButton[] playerButtons = new ArcadeButton[3];
		int[] buttonIndexes = leftHanded ? getLeftHandedButtonOrder() : new int[] { 0, 1, 2 };
		for (int i = 0; i < 3; i++) {
			playerButtons[i] = buttons[3 * playerIndex + buttonIndexes[i]];
		}
		
		// Set the buttons
		players[playerIndex].setButtons(playerButtons);
	}
	
	/**
	 * Override this support different button assignments for left-handed
	 * players.
	 * 
	 * @return
	 */
	
	protected int[] getLeftHandedButtonOrder() {
		return new int[] { 0, 1, 2 };
	}
	
	protected int getGameWidth() {
		return gameWidth;
	}
	
	protected int getGameHeight() {
		return gameHeight;
	}
	
	/**
	 * Reset all the game objects, then start the game timer.
	 */
	abstract public void newGame();
	
	/**
	 * Reset the game objects, but not overall scores etc. for a new round.
	 */
	abstract public void newRound();
	
	/**
	 * Do end-of-round things.
	 */
	abstract public void roundOver();
	
	/**
	 * Stop the game timer and display the winner of the last game.
	 */
	abstract public void gameOver();
	
	private void setDemoMode(boolean demoMode) {
		this.demoMode = demoMode;
		
		// Start a new game when leaving demo mode
		if (!this.demoMode) {
			newGame();
		}
	}
	
	public void togglePause() {
		if (gameLoopThread != null && gameLoopThread.isAlive()) {
			stopGameLoop();
		} else {
			startGameLoop();
		}
	}
	
	abstract public long getPhysicsTickMillis();
	
	abstract protected void physicsTick();
	
	abstract protected void drawVideoFrame(Graphics g);
	
	public void render() {
		drawVideoFrame(videoFrame.createGraphics());
		repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int xDraw = (getWidth() - videoWidth) / 2;
		int yDraw = 0;
		
		if (videoScale == 1) {
			g.drawImage(videoFrame, xDraw, yDraw, null);
		} else {
			g.drawImage(videoFrame.getScaledInstance(videoWidth,
					videoHeight, Image.SCALE_FAST), xDraw, yDraw, null);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
		case "GameTick":
			physicsTick();
			render();
			break;
		
		case "DemoMode":
			setDemoMode(true);
			demoTimer.stop();
			break;
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		for (ArcadeButton button : buttons) {
			button.keyTyped(e);
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		// Restart the demo timer
		demoTimer.restart();
		
		// Exit demo mode
		if (demoMode) {
			setDemoMode(false);
		}
		
		switch (e.getKeyCode()) {
		case KeyEvent.VK_Q:
			setPlayerHand(0, true);
			break;
		case KeyEvent.VK_W:
			setPlayerHand(0, false);
			break;
		case KeyEvent.VK_U:
			setPlayerHand(1, true);
			break;
		case KeyEvent.VK_I:
			setPlayerHand(1, false);
			break;
		case KeyEvent.VK_ESCAPE:
			System.exit(0);
			break;
		default:
			for (ArcadeButton button : buttons) {
				button.keyPressed(e);
			}
			break;
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		for (ArcadeButton button : buttons) {
			button.keyReleased(e);
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// togglePause();
	}
	
	@Override
	public void mousePressed(MouseEvent e) {}
	
	@Override
	public void mouseReleased(MouseEvent e) {}
	
	@Override
	public void mouseEntered(MouseEvent e) {}
	
	@Override
	public void mouseExited(MouseEvent e) {}
}
