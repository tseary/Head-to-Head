package headtohead;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import javax.swing.Timer;

import button.ArcadeButton;
import button.IButton;
import button.InputSource;
import button.VirtualButton;
import sound.SoundPlayer;

public abstract class HeadToHeadGameCanvas extends Canvas
		implements ActionListener, KeyListener, MouseListener {
	private static final long serialVersionUID = 1L;
	
	protected ArcadeButton[] buttons;
	protected Player[] players;
	
	private final float aspectRatio = 4f / 3f;
	/** The resolution of the game in pixels. */
	private int gameWidth, gameHeight;
	
	private int videoScale;
	protected BufferedImage videoFrame;
	
	/** The size of the output video on-screen in pixels. */
	protected int videoWidth, videoHeight;
	
	// Game loop
	private GameLoop gameLoopRunnable;
	private Thread gameLoopThread;
	
	// Demo mode
	// TODO Remove game-level demo mode. If this timer expires, go back to the selection screen.
	// TODO Periodically show demos of each game on the selection screen.
	private int demoIdleTime = 60000; // Go to demo mode after 60s of inactivity
	protected Timer demoTimer;
	protected boolean demoMode = false;
	
	// Sound
	protected SoundPlayer sound;
	
	public HeadToHeadGameCanvas(int newGameWidth) {
		// Calculate the size of the video
		gameWidth = newGameWidth;
		gameHeight = (int)(gameWidth * aspectRatio);
		initializeVideoScale();
		
		// Set up the panel
		setPreferredSize(new Dimension(videoWidth, videoHeight));
		setBackground(Color.BLACK);
		setFocusable(false);
		
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
		
		// Create the game loop, with the actual refresh rate if it is available
		int refreshRate = getDisplayRefreshRate();
		if (refreshRate != DisplayMode.REFRESH_RATE_UNKNOWN) {
			gameLoopRunnable = new GameLoop(this, refreshRate);
		} else {
			gameLoopRunnable = new GameLoop(this);
		}
		
		if (DebugMode.isEnabled()) {
			try {
				PrintStream stream = new PrintStream("debug.txt");
				stream.println("refresh rate = " + refreshRate);
				stream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			demoIdleTime = 10000;
		}
		
		// Demo mode
		demoTimer = new Timer(demoIdleTime, this);
		demoTimer.setActionCommand("DemoMode");
		demoTimer.setRepeats(false);
		
		// Sound
		sound = new SoundPlayer();
	}
	
	private static int getDisplayRefreshRate() {
		// Get the screen parameters
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screenDevices = ge.getScreenDevices();
		
		DisplayMode displayMode = screenDevices[0].getDisplayMode();
		return displayMode.getRefreshRate();
	}
	
	/** Calculates the best size for the video based on the screen size. */
	private void initializeVideoScale() {
		// Get the screen size
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		videoScale = Math.min((int)screenSize.getWidth() / gameWidth,
				(int)screenSize.getHeight() / gameHeight);
		videoScale = Math.max(videoScale, 1);
		
		// Set the video size
		videoWidth = videoScale * gameWidth;
		videoHeight = videoScale * gameHeight;
	}
	
	/**
	 * Creates and populates the array of arcade buttons, with hard-coded key
	 * codes: { A, S, D, J, K, L }
	 */
	protected void initializeButtons() {
		buttons = new ArcadeButton[6];
		
		int[] buttonKeyCodes = new int[] {
				KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D,
				KeyEvent.VK_J, KeyEvent.VK_K, KeyEvent.VK_L };
		
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new ArcadeButton(buttonKeyCodes[i]);
		}
	}
	
	/**
	 * Creates the array of players. Gives each player their color and arcade
	 * buttons. Call super.initializePlayers() if overriding this function.
	 */
	protected void initializePlayers() {
		players = new Player[2];
		
		InputSource player0InputSource = new InputSource(new IButton[] {
				buttons[0], buttons[1], buttons[2] });
		InputSource player1InputSource = new InputSource(new IButton[] {
				buttons[3], buttons[4], buttons[5] });
		
		players[0] = new Player(player0InputSource, new Color(0xff1220)); // Red
		players[1] = new Player(player1InputSource, new Color(0xf7e700)); // Yellow
	}
	
	/**
	 * Starts the game loop Runnable in a new thread.
	 * Also starts the demo timer.
	 * @throws InterruptedException
	 */
	public void startGameLoop() throws InterruptedException {
		startGameLoop(true);
	}
	
	/**
	 * Starts the game loop Runnable in a new thread.
	 * @param enableDemo If true, the demo timer is started.
	 * @throws InterruptedException
	 */
	public void startGameLoop(boolean enableDemo) throws InterruptedException {
		stopGameLoop();
		
		// Start a new thread
		gameLoopThread = new Thread(gameLoopRunnable);
		gameLoopThread.start();
		
		// Start the demo timer if we are not in demo mode already
		if (!demoMode && enableDemo) {
			demoTimer.start();
		}
	}
	
	/**
	 * Interrupts the game loop thread and blocks until it dies.
	 * @throws InterruptedException
	 */
	public void stopGameLoop() throws InterruptedException {
		// Stop the demo timer
		demoTimer.stop();
		
		// Stop the old thread if there is one
		if (gameLoopThread != null && gameLoopThread.isAlive()) {
			gameLoopThread.interrupt();
			gameLoopThread = null;
		}
	}
	
	/**
	 * Blocks until the game loop thread dies.
	 * @throws InterruptedException
	 */
	public void joinGameLoopThread() throws InterruptedException {
		// Join the game loop and null the thread when it dies
		if (gameLoopThread != null && gameLoopThread.isAlive()) {
			gameLoopThread.join();
			gameLoopThread = null;
		}
	}
	
	/**
	 * Re-orders a player's buttons to suit a right- or left-handed user.
	 * 
	 * @param playerIndex
	 *            The array index of the player.
	 * @param leftHanded
	 *            True if the user is left-handed, false if they are
	 *            right-handed.
	 */
	protected void setPlayerHand(int playerIndex, boolean leftHanded) {
		InputSource playerInputSource = players[playerIndex].getInputSource();
		
		// If the player doesn't have an input source, create an array of buttons in the right-handed order
		/*if (playerInputSource == null) {
			IButton[] playerButtons = new IButton[3];
			for (int i = 0; i < 3; i++) {
				playerButtons[i] = buttons[3 * playerIndex + i];
			}
			players[playerIndex].setInputSource(new InputSource(playerButtons));
		}*/
		
		// Remap the buttons
		if (leftHanded) {
			playerInputSource.setButtonRemap(getLeftHandedButtonRemap());
		} else {
			playerInputSource.clearButtonRemap();
		}
		
		// TODO Shuffle the press counters too
	}
	
	/**
	 * Override this to support different button assignments for left-handed
	 * players.
	 * 
	 * @return A permutation of the array { 0, 1, 2 }.
	 */
	protected int[] getLeftHandedButtonRemap() {
		return new int[] { 0, 1, 2 };
	}
	
	/**
	 * Gets the width of the game view in pixels.
	 * 
	 * @return
	 */
	protected int getGameWidth() {
		return gameWidth;
	}
	
	/**
	 * Gets the height of the game view in pixels.
	 * 
	 * @return
	 */
	protected int getGameHeight() {
		return gameHeight;
	}
	
	/**
	 * Reset all the game objects.
	 * This function should NOT call startGameLoop().
	 */
	abstract public void newGame();
	
	/**
	 * Reset the game objects, but not overall scores etc. for a new round.
	 */
	abstract public void newRound();
	
	private void setDemoMode(boolean demoMode) {
		this.demoMode = demoMode;
		
		// TODO fix this
		// Set the players to human or computer control
		InputSource player0InputSource, player1InputSource;
		if (this.demoMode) {
			// Computer
			player0InputSource = new DemoInputSource(new IButton[] {
					new VirtualButton(), new VirtualButton(), new VirtualButton() });
			player1InputSource = new DemoInputSource(new IButton[] {
					new VirtualButton(), new VirtualButton(), new VirtualButton() });
		} else {
			// Human
			// TODO This will lose the handedness setting
			player0InputSource = new InputSource(new IButton[] {
					buttons[0], buttons[1], buttons[2] });
			player1InputSource = new InputSource(new IButton[] {
					buttons[3], buttons[4], buttons[5] });
		}
		players[0].setInputSource(player0InputSource);
		players[1].setInputSource(player1InputSource);
		
		// Start a new game when entering or leaving demo mode
		newGame();
	}
	
	public void togglePause() {
		try {
			if (gameLoopThread != null && gameLoopThread.isAlive()) {
				stopGameLoop();
			} else {
				startGameLoop();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the amount of time represented by one call to physicsTick().
	 * Must return a positive number.
	 * @return
	 */
	abstract public long getPhysicsTickMillis();
	
	/**
	 * Execute one cycle of the game physics.
	 * This function should call stopGameLoop() when the game ends.
	 */
	abstract protected void physicsTick();
	
	abstract protected void drawVideoFrame(Graphics g, double extrapolate);
	
	public void render(double extrapolate) {
		drawVideoFrame(videoFrame.createGraphics(), extrapolate);
		BufferStrategy strategy = getBufferStrategy();
		if (strategy != null) {
			paint(strategy.getDrawGraphics());
			strategy.show();
		} else {
			repaint();
		}
	}
	
	@Override
	public void paint(Graphics g) {
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
			case "DemoMode":
				setDemoMode(true);
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
		if (demoMode) {
			// Exit demo mode
			setDemoMode(false);
		} else {
			// Restart the demo timer
			if (demoTimer.isRunning()) {
				demoTimer.restart();
			}
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
