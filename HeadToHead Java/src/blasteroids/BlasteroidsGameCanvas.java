package blasteroids;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import button.ArcadeButton;
import button.IButton;
import geometry.Vector2D;
import geometry.Vector2DLong;
import headtohead.HeadToHeadGameCanvas;
import headtohead.IOwnable;
import headtohead.IScorable;
import headtohead.Player;
import physics.IPolygon;
import physics.PhysicsConstants;
import physics.PhysicsObject;
import sound.SoundName;

public class BlasteroidsGameCanvas extends HeadToHeadGameCanvas {
	private static final long serialVersionUID = 1L;
	
	// Physics constants
	private static final int gameTimerFPS = 60;
	private static final double spaceshipMaxSpeed = PhysicsConstants.velocity(200d);
	private static final double spaceshipThrust = PhysicsConstants.acceleration(180d);
	private static final double spaceshipDrag = PhysicsConstants.integral(0.3d);
	private static final double stepsPerHalfTurn = Math.round(gameTimerFPS / 3d);
	private static final double asteroidMinSpeed = PhysicsConstants.velocity(15d),
			asteroidMaxSpeed = PhysicsConstants.velocity(40d);
	private static final double bulletMaxAge = PhysicsConstants.time(2.333d);
	protected long deltaTimeAlive, deltaTimeDead;
	
	@Override
	public long getPhysicsTickMillis() {
		return 1000 / gameTimerFPS;
	}
	
	// Physics objects
	protected Spaceship[] spaceships;
	protected List<Asteroid> asteroids;
	protected List<Bullet> bullets;
	protected List<Fragment> fragments;
	
	// Game timing
	private static final int roundStartTicks = gameTimerFPS;
	private int roundStartCounter;
	private static final int roundOverTicks = (int)(1d * gameTimerFPS);
	private static final int gameOverTicks = roundOverTicks + (int)(2.5d * gameTimerFPS);
	private static final int roundsPerGame = 3;
	private int roundOverCounter;
	private int round;
	private boolean gameOverSoundPlayed;
	
	// Buttons
	protected final int BUTTON_LEFT = 0, BUTTON_RIGHT = 1, BUTTON_SHOOT = 2;
	protected int[] lastShotCounters;
	protected boolean[] shootWasPressed;
	
	// Background
	private long starsRandomSeed = 0;
	
	// Score/text
	private static final long scoreMarkerMaxAge = PhysicsConstants.time(0.5d);
	/** Ephemeral markers that show points as they are earned. */
	protected List<ScoreMarker> scoreMarkers;
	/** Permanent markers that show each player's score. */
	protected List<ScoreMarker> playerScoreMarkers;
	
	public BlasteroidsGameCanvas() {
		super(400);
		
		// Create lists
		spaceships = new Spaceship[players.length];
		asteroids = new ArrayList<Asteroid>();
		bullets = new ArrayList<Bullet>();
		fragments = new ArrayList<Fragment>();
		
		lastShotCounters = new int[players.length];
		shootWasPressed = new boolean[players.length];
		
		scoreMarkers = new ArrayList<ScoreMarker>();
	}
	
	@Override
	protected void initializePlayers() {
		super.initializePlayers();
		
		// Create score markers
		playerScoreMarkers = new ArrayList<ScoreMarker>();
		for (int i = 0; i < players.length; i++) {
			Player player = players[i];
			
			Vector2DLong position1 = new Vector2DLong(
					(long)((i != 0 ? 0.07d : 0.93d) * getGameWidthPhysics()),
					(long)((i != 0 ? 0.90d : 0.10d) * getGameHeightPhysics()));
			playerScoreMarkers.add(new ScoreMarker(String.valueOf(0),
					position1, player, isPlayerInverted(player)));
			
			Vector2DLong position2 = new Vector2DLong(
					(long)((i != 0 ? 0.93d : 0.07d) * getGameWidthPhysics()),
					(long)((i != 0 ? 0.10d : 0.90d) * getGameHeightPhysics() +
							PhysicsConstants.distance(i != 0 ? 15 : -15)));
			playerScoreMarkers.add(new ScoreMarker(String.valueOf(0),
					position2, player, !isPlayerInverted(player)));
		}
	}
	
	@Override
	protected int[] getLeftHandedButtonRemap() {
		return new int[] { 1, 2, 0 };
	}
	
	@Override
	public void newGame() {
		// Reset the objects for a new round
		round = 0;
		for (int i = 0; i < spaceships.length; i++) {
			spaceships[i] = null;
		}
		newRound();
		
		gameOverSoundPlayed = false;
		
		// Randomize the stars
		starsRandomSeed = System.currentTimeMillis();
		
		// Reset player scores
		for (Player player : players) {
			setPlayerScore(player, 0);
		}
		
		// Reset all button press counters
		for (IButton button : buttons) {
			button.resetPressCounter();
		}
		
		// Start the game timer
		deltaTimeAlive = getPhysicsTickMillis();
		deltaTimeDead = deltaTimeAlive / 4;
	}
	
	@Override
	public void newRound() {
		// Create player ships
		for (int i = 0; i < players.length; i++) {
			Spaceship spaceship = spaceships[i];
			
			if (spaceship != null && spaceship.isAlive()) {
				// Refill health
				spaceship.setAlive(true);
			} else {
				// Make new ship
				spaceship = new Spaceship((0.25d + 0.5d * i) * 2d * Math.PI, players[i]);
				spaceship.position.x = getGameWidthPhysics() / 2;
				spaceship.position.y = getGameHeightPhysics() * (1 + 8 * i) / 10;
				spaceships[i] = spaceship;
			}
		}
		
		// Reset shot counters
		for (int i = 0; i < players.length; i++) {
			players[i].getButton(BUTTON_SHOOT).resetPressCounter();
			lastShotCounters[i] = 0;
		}
		
		// Create asteroids
		asteroids.clear();
		Random random = new Random();
		for (int i = 0; i < 3 + round; i++) {
			boolean bigOne = random.nextDouble() < 0.10d;
			
			Asteroid asteroid = new Asteroid(bigOne ? 3 : 2);
			
			// Randomize position
			final long minRadiusSqr = (long)Math.pow(
					0.15d * getGameWidthPhysics() + asteroid.getRadius(), 2d);
			System.out.println("minRadiusSqr = " + minRadiusSqr);
			boolean validPosition;
			do {
				asteroid.position.x = random.nextInt((int)getGameWidthPhysics());
				asteroid.position.y = random.nextInt((int)getGameHeightPhysics());
				
				// Test the position and reroll if it's invalid
				validPosition = true;
				for (int p = 0; p < spaceships.length; p++) {
					if (asteroid.distanceSquaredTo(spaceships[p]) > minRadiusSqr) continue;
					validPosition = false;
					break;
				}
			} while (!validPosition);
			
			asteroid.velocity = new Vector2D(random.nextDouble() *
					(asteroidMaxSpeed - asteroidMinSpeed) + asteroidMinSpeed,
					2d * Math.PI * random.nextDouble(), true);
			
			asteroids.add(asteroid);
			
			if (bigOne) {
				i++;
			}
		}
		System.out.println();
		
		// Clear lists
		bullets.clear();
		fragments.clear();
		scoreMarkers.clear();
		sound.clearRequests();
		
		// Reset the round counters
		roundStartCounter = 0;
		roundOverCounter = 0;
		
		round++;
	}
	
	/**
	 * Returns true if any button's press counter is greater than zero.
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean wasAnyButtonPressed() {
		for (ArcadeButton button : buttons) {
			if (button.getPressCounter() > 0) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void physicsTick() {
		try {
			roundStartCounter++;
			
			// Do the game tick
			if (!isRoundOver()) {
				physicsTickAlive();
			} else {
				physicsTickDead();
			}
			
			// Play all the sounds that were requested during this tick
			sound.playRequestedSounds();
		} catch (Exception ex) {
			ex.printStackTrace();
			newGame();
		}
	}
	
	/**
	 * The normal game tick.
	 */
	private void physicsTickAlive() {
		// Physics stuff
		setThrusts();
		moveEverything(deltaTimeAlive);
		ageScoreMarkers(deltaTimeAlive);
		shootBullets();
		
		collideSpaceshipToSpaceship();
		collideBulletToSpaceship();
		collideAsteroidToSpaceship();
		
		collideAsteroidToAsteroid();
		
		collideBulletToAsteroid();
	}
	
	/**
	 * The game tick after someone has died.
	 */
	private void physicsTickDead() {
		// Check the round end counter
		if (checkRoundEndCounter()) {
			return;
		}
		
		// Physics stuff
		setThrusts();
		moveEverything(deltaTimeDead);
		ageScoreMarkers(deltaTimeAlive);
		shootBullets();
		
		// No more spaceship or bullet collisions after someone is dead
		collideAsteroidToAsteroid();
	}
	
	/**
	 * 
	 * @return True if a new round or new game was started.
	 */
	private boolean checkRoundEndCounter() {
		if (++roundOverCounter >= roundOverTicks) {
			// Round over time has elapsed
			if (demoMode) {
				// Start a new game for demo mode
				newGame();
				return true;
			}
			
			// It's not the final round if the scores are tied
			boolean finalRound = round >= roundsPerGame &&
					players.length >= 2 && !(players[0].score == players[1].score);
			
			if (finalRound) {
				if (roundOverCounter >= gameOverTicks) {
					// Game over time has elapsed
					try {
						stopGameLoop();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return true;
				}
				
				// Play the game over sound once
				if (!gameOverSoundPlayed) {
					sound.request(SoundName.GAMEOVER);
					gameOverSoundPlayed = true;
				}
			} else {
				newRound();
				return true;
			}
		}
		return false;
	}
	
	private void setThrusts() {
		// Read buttons, set spaceship thrusts
		for (int i = 0; i < players.length; i++) {
			Player player = players[i];
			Spaceship spaceship = spaceships[i];
			
			// Apply drag always
			spaceship.acceleration = spaceship.velocity
					.scalarProduct(-spaceshipDrag);
			
			// Skip if dead
			if (!spaceship.isAlive()) {
				continue;
			}
			
			// Apply thrust when shoot is held down
			boolean shootPressed = player.getButton(BUTTON_SHOOT).isPressed();
			boolean thrustOn = shootPressed && shootWasPressed[i];
			if (thrustOn) {
				spaceship.acceleration.add(
						new Vector2D(spaceshipThrust, spaceship.angle, true));
			}
			shootWasPressed[i] = shootPressed;
			
			// Turn
			if (player.getButton(BUTTON_LEFT).isPressed()) {
				spaceship.angle -= Math.PI / stepsPerHalfTurn;
			} else if (player.getButton(BUTTON_RIGHT).isPressed()) {
				spaceship.angle += Math.PI / stepsPerHalfTurn;
			}
		}
	}
	
	private void moveEverything(long deltaTime) {
		// Move asteroids
		for (Asteroid asteroid : asteroids) {
			asteroid.move(deltaTime);
			asteroid.wrapPosition(getGameWidthPhysics(), getGameHeightPhysics());
		}
		
		// Move all spaceships
		for (Spaceship spaceship : spaceships) {
			spaceship.move(deltaTime);
			
			// Clamp the speed
			if (spaceship.velocity.length() > spaceshipMaxSpeed) {
				spaceship.velocity.setLength(spaceshipMaxSpeed);
			}
			
			spaceship.wrapPosition(getGameWidthPhysics(), getGameHeightPhysics());
		}
		
		// Move bullets
		for (int i = 0; i < bullets.size(); i++) {
			Bullet bullet = bullets.get(i);
			bullet.move(deltaTime);
			
			// Remove the bullet if it is too old
			if (bullet.getAge() > bulletMaxAge) {
				bullets.remove(i--);
				continue;
			}
			
			bullet.wrapPosition(getGameWidthPhysics(), getGameHeightPhysics());
		}
		
		// Move fragments
		for (Fragment fragment : fragments) {
			fragment.move(deltaTime);
		}
	}
	
	private void ageScoreMarkers(long deltaTime) {
		// Age score markers and remove ones that are too old
		for (int i = 0; i < scoreMarkers.size(); i++) {
			ScoreMarker scoreMarker = scoreMarkers.get(i);
			scoreMarker.move(deltaTime);
			if (scoreMarkers.get(i).getAge() > scoreMarkerMaxAge) {
				scoreMarkers.remove(i--);
			}
		}
	}
	
	private void shootBullets() {
		// Shoot new bullets
		for (int i = 0; i < players.length; i++) {
			Player player = players[i];
			
			// Cannot shoot when dead
			if (!spaceships[i].isAlive()) {
				continue;
			}
			
			// Shoot a single bullet if the press counter increased
			int shotCounter = player.getButton(BUTTON_SHOOT)
					.getPressCounter();
			if (shotCounter > lastShotCounters[i]) {
				Bullet shot = spaceships[i].shoot();
				if (shot != null) {
					bullets.add(shot);
					sound.request(SoundName.PEW);
				}
				lastShotCounters[i] = shotCounter;
			}
		}
	}
	
	private void collideSpaceshipToSpaceship() {
		// Calculate spaceship-spaceship collisions
		for (int a = 0; a < spaceships.length - 1; a++) {
			Spaceship spaceshipA = spaceships[a];
			if (!spaceshipA.isAlive()) {
				continue;
			}
			
			for (int b = a + 1; b < spaceships.length; b++) {
				Spaceship spaceshipB = spaceships[b];
				if (!spaceshipB.isAlive()) {
					continue;
				}
				
				if (spaceshipA.isTouching(spaceshipB)) {
					// Both are dead
					spaceshipA.setAlive(false);
					spaceshipB.setAlive(false);
					
					spaceshipDied(spaceshipA);
					spaceshipDied(spaceshipB);
					
					givePoints(spaceshipA, spaceshipB);
					givePoints(spaceshipB, spaceshipA);
					
					break; // Break to prevent a dead ship from colliding again
				}
			}
		}
	}
	
	private void collideBulletToSpaceship() {
		// Calculate spaceship-bullet collisions
		for (Spaceship spaceship : spaceships) {
			if (!spaceship.isAlive()) {
				continue;
			}
			
			for (int i = 0; i < bullets.size(); i++) {
				Bullet bullet = bullets.get(i);
				
				// No friendly fire
				if (bullet.getOwner() == spaceship.getOwner()) {
					continue;
				}
				
				// Bullet hits the spaceship
				if (bullet.isTouching(spaceship)) {
					spaceship.takeHit();
					
					// Bullet owner gets points
					givePoints(bullet, spaceship);
					
					if (spaceship.isAlive()) {
						sound.request(SoundName.HIT);
					} else {
						spaceshipDied(spaceship);
					}
					
					// Remove the bullet from the list
					bullets.remove(i--);
					break;
				}
			}
		}
	}
	
	private void collideAsteroidToSpaceship() {
		// Calculate spaceship-asteroid collisions
		for (Spaceship spaceship : spaceships) {
			if (!spaceship.isAlive()) {
				continue;
			}
			
			for (Asteroid asteroid : asteroids) {
				if (spaceship.isTouchingWrapped(asteroid, getGameWidthPhysics(), getGameHeightPhysics())) {
					// Bounce off the asteroid and lose a life
					// No points are awarded
					
					// Ship and asteroid bounce off each other
					sound.request(SoundName.BUMP);
					asteroid.bounceWrapped(spaceship, getGameWidthPhysics(), getGameHeightPhysics());
					
					// TODO Apply random angular velocity
					// spaceship.angularVelocity += 1d;
					
					// Ship gets hurt
					spaceship.takeHit();
					
					if (spaceship.isAlive()) {
						sound.request(SoundName.BOUNCE);
					} else {
						spaceshipDied(spaceship);
					}
					
					break; // Break to prevent hitting multiple asteroids
				}
			}
		}
	}
	
	private void collideAsteroidToAsteroid() {
		// Calculate asteroid-asteroid collisions
		for (int a = 0; a < asteroids.size() - 1; a++) {
			Asteroid asteroidA = asteroids.get(a);
			for (int b = a + 1; b < asteroids.size(); b++) {
				Asteroid asteroidB = asteroids.get(b);
				if (asteroidA.isTouchingWrapped(asteroidB, getGameWidthPhysics(), getGameHeightPhysics())) {
					// Asteroids bounce off each other
					sound.request(SoundName.BUMP);
					asteroidA.bounceWrapped(asteroidB, getGameWidthPhysics(), getGameHeightPhysics());
				}
			}
		}
	}
	
	private void collideBulletToAsteroid() {
		// Calculate bullet-asteroid collisions
		for (int a = 0; a < asteroids.size(); a++) {
			Asteroid asteroid = asteroids.get(a);
			for (int i = 0; i < bullets.size(); i++) {
				Bullet bullet = bullets.get(i);
				if (bullet.isTouchingWrapped(asteroid, getGameWidthPhysics(), getGameHeightPhysics())) {
					// Bullet hits this asteroid
					sound.request(SoundName.CRACK);
					givePoints(bullet, asteroid);
					
					// Split the asteroid in two
					Asteroid otherHalf = asteroid.split(bullet);
					if (otherHalf != null) {
						asteroids.add(otherHalf);
					} else {
						// The asteroid was destroyed, remove it from the list
						asteroids.remove(a--);
					}
					
					// Remove this bullet from the list
					bullets.remove(i--);
					break;
				}
			}
		}
	}
	
	private void spaceshipDied(Spaceship spaceship) {
		fragments.addAll(spaceship.getFragments());
		sound.request(SoundName.CRASH);
	}
	
	private void givePoints(IOwnable playerObj, IScorable scoreObj) {
		Player owner = playerObj.getOwner();
		if (owner == null) {
			return;
		}
		
		// Add points and update score marker
		int score = scoreObj.getScore();
		setPlayerScore(owner, owner.score + score);
		
		// Create a score marker if the object is physical
		if (scoreObj instanceof PhysicsObject) {
			PhysicsObject physicsObj = (PhysicsObject)scoreObj;
			scoreMarkers.add(new ScoreMarker(String.valueOf(score),
					physicsObj.position, owner, isPlayerInverted(owner)));
		}
	}
	
	@SuppressWarnings("unused")
	private void takePoints(IOwnable playerObj, IScorable scoreObj, PhysicsObject physicsObj) {
		Player owner = playerObj.getOwner();
		if (owner == null) {
			return;
		}
		
		// Subtract points and update score marker
		int score = -scoreObj.getScore() / 2;
		setPlayerScore(owner, owner.score + score);
		
		// Create a score marker if the object is physical
		if (physicsObj != null) {
			scoreMarkers.add(new ScoreMarker(String.valueOf(score),
					physicsObj.position, null/*owner*/, isPlayerInverted(owner)));
		}
	}
	
	/**
	 * Sets a player's score to the given value and updates the score markers.
	 * 
	 * @param player
	 * @param newScore
	 */
	private void setPlayerScore(Player player, int newScore) {
		player.score = Math.max(newScore, 0);
		for (ScoreMarker scoreMarker : playerScoreMarkers) {
			if (scoreMarker.getOwner() == player) {
				scoreMarker.value = String.valueOf(player.score);
			}
		}
	}
	
	private boolean isPlayerInverted(Player player) {
		return player == players[0];
	}
	
	public boolean isRoundOver() {
		for (int i = 0; i < players.length; i++) {
			if (!spaceships[i].isAlive()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void drawVideoFrame(Graphics g, double extrapolate) {
		// Scale by the delta time
		long extrapolateTime = (long)(extrapolate *
				(isRoundOver() ? deltaTimeDead : deltaTimeAlive));
		
		// Clear the frame
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getGameWidthPixels(), getGameHeightPixels());
		
		// It's full of stars
		Random random = new Random(starsRandomSeed);
		final Color color1 = Color.GRAY,
				color2 = Color.CYAN.darker();
		for (int i = 0; i < 80; i++) {
			g.setColor(random.nextBoolean() ? color1 : color2);
			int starSize = random.nextBoolean() ? 1 : 2;
			g.fillRect(random.nextInt(getGameWidthPixels()),
					random.nextInt(getGameHeightPixels()), starSize, starSize);
		}
		
		// Draw the asteroids
		g.setColor(Color.GRAY);
		for (Asteroid asteroid : asteroids) {
			drawPhysicsObject(g, asteroid, extrapolateTime, true);
		}
		
		// Draw the player spaceships
		for (Spaceship spaceship : spaceships) {
			if (!spaceship.isAlive()) {
				continue;
			}
			g.setColor(getOwnerColor(spaceship));
			drawPolygon(g, spaceship, extrapolateTime);
		}
		
		// Draw the spaceship fragments
		for (Fragment fragment : fragments) {
			g.setColor(getOwnerColor(fragment));
			drawPolygon(g, fragment, extrapolateTime);
		}
		
		// Draw the bullets
		for (Bullet bullet : bullets) {
			g.setColor(getOwnerColor(bullet));
			drawPhysicsObject(g, bullet, extrapolateTime);
		}
		
		// Draw score markers
		for (ScoreMarker scoreMarker : scoreMarkers) {
			drawScoreMarker(g, scoreMarker);
		}
		for (ScoreMarker scoreMarker : playerScoreMarkers) {
			drawScoreMarker(g, scoreMarker);
		}
		
		// Draw health markers
		for (int i = 0; i < players.length; i++) {
			g.setColor(players[i].getColor());
			
			final int triangleWidth = 8, triangleHeight = 11, triangleSpacing = 15;
			
			int yBase = i == 0 ? (getGameHeightPixels() / 10 - 6) : (getGameHeightPixels() * 9 / 10 + 6);
			int yNose = yBase + (i == 0 ? triangleHeight : -triangleHeight);
			int xFirst = i == 0 ? 50 : (getGameWidthPixels() - (50 + triangleWidth) - 1);
			int xPerHealth = i == 0 ? -triangleSpacing : triangleSpacing;
			
			for (int h = 0; h < spaceships[i].getHealth(); h++) {
				int xHealth = xPerHealth * h;
				Polygon triangle = new Polygon(
						new int[] { xFirst + xHealth,
								xFirst + triangleWidth / 2 + xHealth,
								xFirst + triangleWidth + xHealth },
						new int[] { yBase, yNose, yBase }, 3);
				// g.fillPolygon(triangle);
				g.drawPolygon(triangle);
			}
		}
		
		// Draw text on top of everything
		final int yLine1 = getGameHeightPixels() / 5;
		final int yLine2 = yLine1 - 15;
		if (demoMode) {
			drawTextMarker(g, "DEMO", yLine1);
			drawTextMarker(g, "PRESS ANY BUTTON TO START", yLine2);
		} else if (roundStartCounter < roundStartTicks) {
			String startString = round > roundsPerGame
					? "OVERTIME"
					: String.format("ROUND %d OF %d", round, roundsPerGame);
			drawTextMarker(g, startString, yLine1);
		} else if (roundOverCounter > 0 && roundOverCounter <= roundOverTicks) {
			drawTextMarker(g, String.format("ROUND %0$d OVER", round), yLine1);
		} else if (roundOverCounter > roundOverTicks && roundOverCounter <= gameOverTicks) {
			drawTextMarker(g, "GAME OVER", yLine1);
			
			boolean player0Wins = players[0].score > players[1].score;
			boolean player1Wins = players[1].score > players[0].score;
			
			if (player0Wins) {
				drawTextMarker(g, new String[] { "YOU WIN!", "YOU LOSE!" }, yLine2, players[0]);
			} else if (player1Wins) {
				drawTextMarker(g, new String[] { "YOU LOSE!", "YOU WIN!" }, yLine2, players[1]);
			} else {
				// Tie
				// (We never come here, because overtime is implemented)
				drawTextMarker(g, "IT'S A TIE!", yLine2);
			}
		}
	}
	
	private static Color getOwnerColor(IOwnable ownable) {
		Player owner = ownable.getOwner();
		if (owner != null) {
			return owner.getColor();
		}
		return Color.WHITE;
	}
	
	/**
	 * Draws a physics object as a circle.
	 * 
	 * @param g
	 * @param obj
	 */
	private void drawPhysicsObject(Graphics g, PhysicsObject obj, long extrapolateTime) {
		drawPhysicsObject(g, obj, extrapolateTime, false);
	}
	
	private void drawPhysicsObject(Graphics g, PhysicsObject obj, long extrapolateTime, boolean wrap) {
		
		Vector2DLong drawPositionPx = PhysicsConstants.distanceToPixels(
				IPolygon.extrapolatePosition(obj, extrapolateTime));
		
		int radius = Math.max(1, PhysicsConstants.distanceToPixels(obj.getRadius()));
		int xDraw = (int)drawPositionPx.x - radius;
		int yDraw = (int)drawPositionPx.y - radius;
		int diameter = 2 * radius;
		
		g.fillOval(xDraw, yDraw, diameter, diameter);
		g.drawOval(xDraw, yDraw, diameter, diameter);
		
		if (!wrap) {
			return;
		}
		
		int xOffset = 0, yOffset = 0;
		
		// Draw wrapped copies of the object
		boolean nearLeft = drawPositionPx.x < radius,
				nearRight = drawPositionPx.x > getGameWidthPixels() - radius;
		if (nearLeft) {
			xOffset = getGameWidthPixels();
		} else if (nearRight) {
			xOffset = -getGameWidthPixels();
		}
		
		boolean nearTop = drawPositionPx.y < radius,
				nearBottom = drawPositionPx.y > getGameHeightPixels() - radius;
		if (nearTop) {
			yOffset = getGameHeightPixels();
		} else if (nearBottom) {
			yOffset = -getGameHeightPixels();
		}
		
		// Draw x wrapped
		if (xOffset != 0) {
			g.fillOval(xDraw + xOffset, yDraw, diameter, diameter);
			g.drawOval(xDraw + xOffset, yDraw, diameter, diameter);
		}
		
		// Draw y wrapped
		if (yOffset != 0) {
			g.fillOval(xDraw, yDraw + yOffset, diameter, diameter);
			g.drawOval(xDraw, yDraw + yOffset, diameter, diameter);
		}
		
		// Draw x and y wrapped
		if (xOffset != 0 && yOffset != 0) {
			g.fillOval(xDraw + xOffset, yDraw + yOffset, diameter, diameter);
			g.drawOval(xDraw + xOffset, yDraw + yOffset, diameter, diameter);
		}
	}
	
	private static void drawPolygon(Graphics g, IPolygon polygonObj, long extrapolateTime) {
		// Get the spaceship outline as a polygon
		Polygon polygon = polygonObj.getOutline(extrapolateTime);
		
		// Fill and draw outline
		g.fillPolygon(polygon);
		g.drawPolygon(polygon);
	}
	
	/**
	 * Draws neutral score markers with arbitrary text, visible to both players.
	 */
	private void drawTextMarker(Graphics g, String message, int yLine) {
		long xCenter = getGameWidthPhysics() / 2;
		long yLinePhys = PhysicsConstants.distance(yLine);
		Vector2DLong position0Inverted = new Vector2DLong(xCenter, yLinePhys);
		Vector2DLong position1NonInverted = new Vector2DLong(xCenter, getGameHeightPhysics() - yLinePhys);
		drawScoreMarker(g, new ScoreMarker(message, position0Inverted, null, true));
		drawScoreMarker(g, new ScoreMarker(message, position1NonInverted, null, false));
	}
	
	/**
	 * Draws score markers with arbitrary text for each player.
	 * 
	 * @param g
	 * @param messages
	 * @param yLine
	 * @param owner
	 */
	private void drawTextMarker(Graphics g, String[] messages, int yLine, Player owner) {
		long xCenter = getGameWidthPhysics() / 2;
		long yLinePhys = PhysicsConstants.distance(yLine);
		Vector2DLong position0Inverted = new Vector2DLong(xCenter, yLinePhys);
		Vector2DLong position1NonInverted = new Vector2DLong(xCenter, getGameHeightPhysics() - yLinePhys);
		drawScoreMarker(g, new ScoreMarker(messages[0], position0Inverted, owner, true));
		drawScoreMarker(g, new ScoreMarker(messages[1], position1NonInverted, owner, false));
	}
	
	private static void drawScoreMarker(Graphics g, ScoreMarker scoreMarker) {
		// Set the color
		g.setColor(getOwnerColor(scoreMarker));
		
		// Center the text
		int xOffset = g.getFontMetrics().stringWidth(scoreMarker.value) / 2,
				yOffset = 5;
		int xDraw = (int)(PhysicsConstants.distanceToPixels(scoreMarker.position.x) +
				(scoreMarker.isInverted() ? xOffset : -xOffset)),
				yDraw = (int)(PhysicsConstants.distanceToPixels(scoreMarker.position.y) -
						(scoreMarker.isInverted() ? yOffset : -yOffset));
		
		// Draw inverted or not
		if (scoreMarker.isInverted()) {
			Graphics2D g2d = (Graphics2D)g;
			g2d.rotate(Math.PI);
			g.drawString(scoreMarker.value, -xDraw, -yDraw);
			g2d.rotate(-Math.PI);
		} else {
			g.drawString(scoreMarker.value, xDraw, yDraw);
		}
	}
}
