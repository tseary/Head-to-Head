package tankbattle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import blasteroids.Bullet;
import blasteroids.Fragment;
import blasteroids.ScoreMarker;
import button.ArcadeButton;
import button.IButton;
import geometry.SpaceVector2DLong;
import geometry.Vector2D;
import geometry.Vector2DLong;
import headtohead.HeadToHeadGameCanvas;
import headtohead.IOwnable;
import headtohead.IScorable;
import headtohead.Performance;
import headtohead.Player;
import physics.IPolygon;
import physics.PhysicsConstants;
import physics.PhysicsObject;
import sound.SoundName;

public class TankBattleGameCanvas extends HeadToHeadGameCanvas {
	private static final long serialVersionUID = 1L;
	
	// Physics constants
	private static final int gameTimerFPS = 60;
	private static final double tankMaxSpeed = PhysicsConstants.velocity(70d);
	private static final double tankThrust = PhysicsConstants.acceleration(180d);
	private static final double tankDrag = PhysicsConstants.integral(2d);
	private static final double tankSteeringAccel = PhysicsConstants.angularAcceleration(8d);
	private static final double tankSteeringDrag = PhysicsConstants.integral(6d);
	private static final double bulletMaxAge = PhysicsConstants.time(3.333d);
	protected long deltaTimeAlive, deltaTimeDead;
	
	@Override
	public long getPhysicsTickMillis() {
		return 1000 / gameTimerFPS;
	}
	
	// Physics objects
	protected Tank[] tanks;
	protected List<Wall> walls;
	protected List<Bullet> bullets;
	protected List<Fragment> fragments;
	
	// Game timing
	private static final int roundStartTicks = gameTimerFPS;
	private int roundStartCounter;
	private static final int roundOverTicks = (int)(2d * gameTimerFPS);
	private static final int gameOverTicks = roundOverTicks + (int)(2.5d * gameTimerFPS);
	private static final int roundsPerGame = 3;
	private int roundOverCounter;
	private int round;
	private boolean gameOverSoundPlayed;
	
	// Buttons
	protected final int BUTTON_LEFT = 0, BUTTON_SHOOT = 1, BUTTON_RIGHT = 2;
	protected int[] lastShotCounters;
	protected boolean[] shootWasPressed;
	
	// Score/text
	private static final double scoreMarkerMaxAge = 0.5d;
	/** Ephemeral markers that show points as they are earned. */
	protected List<ScoreMarker> scoreMarkers;
	/** Permanent markers that show each player's score. */
	protected List<ScoreMarker> playerScoreMarkers;
	
	public TankBattleGameCanvas() {
		super(400);
		
		// Create lists
		tanks = new Tank[players.length];
		walls = new ArrayList<Wall>();
		bullets = new ArrayList<Bullet>();
		fragments = new ArrayList<Fragment>();
		
		Bullet.setRadius(PhysicsConstants.distance(2d));
		
		lastShotCounters = new int[players.length];
		shootWasPressed = new boolean[players.length];
		
		scoreMarkers = new ArrayList<ScoreMarker>();
	}
	
	@Override
	protected void initializePlayers() {
		super.initializePlayers();
		
		players[0].setColor(new Color(0x00c0ff));
		
		// Create score markers
		playerScoreMarkers = new ArrayList<ScoreMarker>();
		for (int i = 0; i < players.length; i++) {
			Player player = players[i];
			
			Vector2DLong position1 = new Vector2DLong(
					(i != 0 ? 0.07d : 0.93d) * getGameWidthPhysics(),
					(i != 0 ? 0.90d : 0.10d) * getGameHeightPhysics());
			playerScoreMarkers.add(new ScoreMarker(String.valueOf(0),
					position1, player, isPlayerInverted(player)));
			
			Vector2DLong position2 = new Vector2DLong(
					(i != 0 ? 0.93d : 0.07d) * getGameWidthPhysics(),
					(i != 0 ? 0.10d : 0.90d) * getGameHeightPhysics() + (i != 0 ? 15 : -15));
			playerScoreMarkers.add(new ScoreMarker(String.valueOf(0),
					position2, player, !isPlayerInverted(player)));
		}
	}
	
	@Override
	protected int[] getLeftHandedButtonRemap() {
		return new int[] { 0, 1, 2 };
	}
	
	@Override
	public void newGame() {
		// Reset the objects for a new round
		round = 0;
		newRound();
		
		gameOverSoundPlayed = false;
		
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
		if (players.length >= 1) {
			Tank player0Tank = new Tank(0.25d * 2d * Math.PI, players[0]);
			player0Tank.position.x = getGameWidthPhysics() / 2;
			player0Tank.position.y = getGameHeightPhysics() / 10;
			tanks[0] = player0Tank;
		}
		
		if (players.length >= 2) {
			Tank player1Tank = new Tank(0.75d * 2d * Math.PI, players[1]);
			player1Tank.position.x = getGameWidthPhysics() / 2;
			player1Tank.position.y = getGameHeightPhysics() * 9 / 10;
			tanks[1] = player1Tank;
		}
		
		// Reset shot counters
		for (int i = 0; i < players.length; i++) {
			players[i].getButton(BUTTON_SHOOT).resetPressCounter();
			lastShotCounters[i] = 0;
		}
		
		// Clear lists
		walls.clear();
		bullets.clear();
		fragments.clear();
		sound.clearRequests();
		scoreMarkers.clear();
		
		// Place walls
		createWalls();
		
		// Reset the round counters
		roundStartCounter = 0;
		roundOverCounter = 0;
		
		round++;
	}
	
	private void createWalls() {
		Random random = new Random();
		
		final int MINIMUM_WALLS = 15;
		final long wallLength = PhysicsConstants.distance(100),
				wallWidth = PhysicsConstants.distance(10),
				wallSpacing = (wallLength - wallWidth) / 2;
		final long clearance = PhysicsConstants.distance(100);
		
		do {
			// Create building
			// Choose a random position, not too close to any tanks
			Vector2DLong buildingCenter = randomPositionNotNearObjects(random, clearance, tanks);
			
			// Rotate the building randomly
			int angleIndex = random.nextInt(6);
			double buildingAngle = angleIndex * Math.PI / 6d;
			
			// Create walls
			for (int i = 0; i < 3; i++) {
				Wall wall = new Wall(random.nextDouble() < 0.30d ? wallLength / 2 : wallLength, wallWidth);
				double angle = buildingAngle + i * Math.PI / 2d;
				wall.position = buildingCenter.sum(
						new Vector2D(wallSpacing, angle, true));
				wall.angle = angle + Math.PI / 2d;
				walls.add(wall);
			}
			
		} while (walls.size() < MINIMUM_WALLS);
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
		Performance.measure("phys stuff");
		
		collideTankToWall();
		collideTankToTank();
		collideBulletToWall();
		collideBulletToTank();
		Performance.measure("collisions");
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
		
		collideTankToWall();
		collideBulletToWall();
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
			
			// Apply drag always
			tanks[i].acceleration = tanks[i].velocity
					.scalarProduct(-tankDrag);
			tanks[i].angularAcceleration = -tanks[i].angularVelocity * tankSteeringDrag;
			
			// Skip if dead
			if (!tanks[i].isAlive()) {
				continue;
			}
			
			// Get skid steer button presses
			boolean leftPressed = player.getButton(BUTTON_LEFT).isPressed();
			boolean rightPressed = player.getButton(BUTTON_RIGHT).isPressed();
			
			if (leftPressed) {
				if (rightPressed) {
					// Forward
					tanks[i].acceleration.add(new Vector2D(tankThrust, tanks[i].angle, true));
				} else {
					// Left
					// tanks[i].angle -= Math.PI / stepsPerHalfTurn;
					tanks[i].angularAcceleration -= tankSteeringAccel;
				}
			} else {
				if (rightPressed) {
					// Right
					// tanks[i].angle += Math.PI / stepsPerHalfTurn;
					tanks[i].angularAcceleration += tankSteeringAccel;
				} else {
					// Stop
				}
			}
			
			// Rotate velocity so that the tanks can't slide sideways
			/*Vector2D unit = new Vector2D(1d, tanks[i].angle, true);
			double velocityMagnitude = tanks[i].velocity.dotProduct(unit);
			tanks[i].velocity = new Vector2D(velocityMagnitude, tanks[i].angle, true);*/
			tanks[i].velocity.setAngle(tanks[i].angle);
		}
	}
	
	private void moveEverything(long deltaTime) {
		// Move all tanks
		for (Tank tank : tanks) {
			tank.move(deltaTime);
			
			// Clamp the speed
			if (tank.velocity.length() > tankMaxSpeed) {
				tank.velocity.setLength(tankMaxSpeed);
			}
			
			// Choose a sound based on the velocity
			double unitVelocity = tank.velocity.length() / tankMaxSpeed,
					unitAngularVel = Math.abs(tank.angularVelocity) *
							tankSteeringDrag / tankSteeringAccel;
			double unitNoise = Math.max(unitVelocity, unitAngularVel);
			if (unitNoise > 0.9d) {
				sound.request(SoundName.ENGINE_3);
			} else if (unitNoise > 0.5d) {
				sound.request(SoundName.ENGINE_2);
			} else if (unitNoise > 0.2d) {
				sound.request(SoundName.ENGINE_1);
			} else if (tank.isAlive()) {
				sound.request(SoundName.ENGINE_IDLE);
			}
			
			tank.wrapPosition(getGameWidthPhysics(), getGameHeightPhysics());
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
			if (!tanks[i].isAlive()) {
				continue;
			}
			
			// Shoot a single bullet if the press counter increased
			int shotCounter = player.getButton(BUTTON_SHOOT)
					.getPressCounter();
			if (shotCounter > lastShotCounters[i]) {
				Bullet shot = tanks[i].shoot();
				if (shot != null) {
					bullets.add(shot);
					sound.request(SoundName.PWANK_C);
				}
				lastShotCounters[i] = shotCounter;
			}
		}
	}
	
	private void collideTankToWall() {
		for (Tank tank : tanks) {
			if (!tank.isAlive()) {
				continue;
			}
			
			for (Wall wall : walls) {
				if (!tank.isTouching(wall)) {
					continue;
				}
				
				// Tank is touching wall - push off in the normal direction
				SpaceVector2DLong surfaceNormal = wall.getSurfaceNormal(tank.position);
				tank.position.add(surfaceNormal.vector);
				
				// Stop tank
				tank.velocity = new Vector2D(0d, 0d);
			}
		}
	}
	
	private void collideTankToTank() {
		// Calculate tank-tank collisions
		for (int a = 0; a < tanks.length - 1; a++) {
			Tank tankA = tanks[a];
			if (!tankA.isAlive()) {
				continue;
			}
			
			for (int b = a + 1; b < tanks.length; b++) {
				Tank tankB = tanks[b];
				if (!tankB.isAlive()) {
					continue;
				}
				
				if (tankA.isTouching(tankB)) {
					// Push
					Vector2DLong positionDifference = tankA.position.difference(tankB.position);
					double radiusSum = tankA.getRadius() + tankB.getRadius();
					double overlap = radiusSum - positionDifference.length();
					/*if (overlap > 0d) {
						positionDifference.setLength(overlap / 2d);
						tankA.position.add(positionDifference);
						tankB.position.subtract(positionDifference);
					}*/
					overlap = Math.max(overlap, 1d);
					positionDifference.setLength(overlap / 2d);
					tankA.position.add(positionDifference);
					tankB.position.subtract(positionDifference);
				}
			}
		}
	}
	
	private void collideBulletToWall() {
		for (Bullet bullet : bullets) {
			for (Wall wall : walls) {
				if (wall.isTouching(bullet)) {
					// Get the normal vector from the wall to the bullet
					SpaceVector2DLong surfaceNormal = wall.getSurfaceNormal(bullet.position);
					
					// Reflect the velocity about the normal
					Vector2D v1 = bullet.velocity,
							v2 = surfaceNormal.vector;
					double k = v1.dotProduct(v2) / v2.dotProduct(v2);
					bullet.velocity.add(v2.scalarProduct(-2d * k));
					
					// Put the bullet on the surface of the wall
					bullet.position = surfaceNormal.position.sum(
							surfaceNormal.vector.scalarProduct(bullet.getRadius()));
					
					// Reduce the life of the bullet
					bullet.addAge(PhysicsConstants.time(0.1d));
					
					// Only collide with one wall to prevent hugging
					break;
				}
			}
		}
	}
	
	private void collideBulletToTank() {
		// Calculate spaceship-bullet collisions
		for (Tank tank : tanks) {
			if (!tank.isAlive()) {
				continue;
			}
			
			for (int i = 0; i < bullets.size(); i++) {
				Bullet bullet = bullets.get(i);
				
				// Bullet hits the tank
				if (tank.isTouching(bullet)) {
					tank.takeHit();
					
					// Bullet owner gets points
					// (or loses point for friendly fire)
					scorePoints(bullet, tank);
					
					if (tank.isAlive()) {
						sound.request(SoundName.PWANK_E);
					} else {
						tankDied(tank);
					}
					
					// Remove the bullet from the list
					bullets.remove(i--);
					break;
				}
			}
		}
	}
	
	private void tankDied(Tank tank) {
		fragments.addAll(tank.getFragments());
		sound.request(SoundName.EXPLODE);
	}
	
	private void scorePoints(IOwnable playerObj, IScorable scoreObj) {
		Player owner = playerObj.getOwner();
		if (owner == null) {
			return;
		}
		
		// Determine if the player scored against themself
		boolean friendlyFire = false;
		if (scoreObj instanceof IOwnable) {
			Player scoredAgainst = ((IOwnable)scoreObj).getOwner();
			friendlyFire = owner == scoredAgainst;	// Self-own
		}
		
		// Add points and update score marker
		int score = scoreObj.getScore();
		if (friendlyFire) score = -score;
		
		setPlayerScore(owner, owner.score + score);
		
		// Create a score marker if the object is physical
		if (scoreObj instanceof PhysicsObject) {
			PhysicsObject physicsObj = (PhysicsObject)scoreObj;
			scoreMarkers.add(new ScoreMarker(String.valueOf(score),
					physicsObj.position, friendlyFire ? null : owner, isPlayerInverted(owner)));
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
		// State of the game
		int numberOfLiving = 0;
		boolean nobodyHasAmmo = true;
		
		for (int i = 0; i < players.length; i++) {
			// The round is over if someone is dead
			if (tanks[i].isAlive()) {
				numberOfLiving++;
			}
			nobodyHasAmmo &= tanks[i].getAmmo() == 0;
		}
		
		// Return true if one or none players are living, or stalemate
		return numberOfLiving <= 1 || (nobodyHasAmmo && bullets.isEmpty());
	}
	
	@Override
	public void drawVideoFrame(Graphics g, double extrapolate) {
		// Scale by the delta time
		long extrapolateTime = (long)(extrapolate *
				(isRoundOver() ? deltaTimeDead : deltaTimeAlive));
		
		// Clear the frame (background colour)
		g.setColor(new Color(0x006000));
		g.fillRect(0, 0, getGameWidthPixels(), getGameHeightPixels());
		
		// Draw the tank fragments
		for (Fragment fragment : fragments) {
			g.setColor(getOwnerColor(fragment));
			drawPolygon(g, fragment, extrapolateTime);
		}
		
		// Draw the walls
		for (Wall wall : walls) {
			g.setColor(Color.LIGHT_GRAY);
			drawPolygon(g, wall, extrapolateTime);
		}
		
		// Draw the player tanks
		for (Tank tank : tanks) {
			if (!tank.isAlive()) {
				continue;
			}
			g.setColor(getOwnerColor(tank));
			drawPhysicsObject(g, tank, extrapolateTime, true);
		}
		
		// DEBUG
		// Draw lines demonstrating surface normals
		/*if (DebugMode.isEnabled() && walls.size() >= 2) {
			Wall wall = walls.get(1);
			for (Bullet bullet : bullets) {
				
				SpaceVector2D surfaceNormal = wall.getSurfaceNormal(bullet.position);
				
				g.setColor(Color.GREEN);
				g.drawLine((int)surfaceNormal.position.x, (int)surfaceNormal.position.y,
						(int)bullet.position.x, (int)bullet.position.y);
				g.setColor(Color.RED);
				Vector2D normalVectorEnd = surfaceNormal.position.sum(surfaceNormal.vector.scalarProduct(30d));
				g.drawLine((int)surfaceNormal.position.x, (int)surfaceNormal.position.y,
						(int)normalVectorEnd.x, (int)normalVectorEnd.y);
			}
		}*/
		
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
			
			final int rectWidth = 8, rectHeight = 11, rectSpacing = 15;
			
			int yBack = i == 0 ? (getGameHeightPixels() / 10 - 6) : (getGameHeightPixels() * 9 / 10 + 6);
			int yFront = yBack + (i == 0 ? rectHeight : -rectHeight);
			int xFirst = i == 0 ? 50 : (getGameWidthPixels() - (50 + rectWidth) - 1);
			int xPerHealth = i == 0 ? -rectSpacing : rectSpacing;
			
			for (int h = 0; h < tanks[i].getHealth(); h++) {
				int xHealth = xPerHealth * h;
				Polygon rectangle = new Polygon(
						new int[] { xFirst + xHealth,
								xFirst + xHealth,
								xFirst + rectWidth + xHealth,
								xFirst + rectWidth + xHealth },
						new int[] { yBack, yFront, yFront, yBack }, 4);
				g.drawPolygon(rectangle);
			}
			
			// DEBUG
			// Draw a line connecting the tanks
			/*if (DebugMode.isEnabled()) {
				g.setColor(Color.BLUE);
				g.drawLine((int)tanks[0].position.x, (int)tanks[0].position.y,
						(int)tanks[1].position.x, (int)tanks[1].position.y);
			}*/
		}
		
		// Draw ammo markers
		for (int i = 0; i < players.length; i++) {
			g.setColor(players[i].getColor());
			
			int barWidth = 2 * tanks[i].getAmmo();
			int barHeight = 6;
			int xBar = i == 0 ? (59 - barWidth) : (getGameWidthPixels() - 59);
			int yBar = i == 0 ? 65 : (getGameHeightPixels() - 65 - barHeight);
			
			for (int h = 0; h < tanks[i].getHealth(); h++) {
				g.fillRect(xBar, yBar, barWidth, barHeight);
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
		
		// Get the shape of the object
		Polygon polygon = null;
		int radius = Math.max(1, PhysicsConstants.distanceToPixels(obj.getRadius()));
		int xDraw = (int)drawPositionPx.x;
		int yDraw = (int)drawPositionPx.y;
		int diameter = 0;
		if (obj instanceof IPolygon) {
			polygon = ((IPolygon)obj).getOutline(extrapolateTime);
		} else {
			xDraw -= radius;
			yDraw -= radius;
			diameter = 2 * radius;
		}
		
		if (polygon != null) {
			renderPolygon(g, polygon);
		} else {
			renderCircle(g, xDraw, yDraw, diameter);
		}
		
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
			if (polygon != null) {
				Polygon xShifted = new Polygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
				xShifted.translate(xOffset, 0);
				renderPolygon(g, xShifted);
			} else {
				renderCircle(g, xDraw + xOffset, yDraw, diameter);
			}
		}
		
		// Draw y wrapped
		if (yOffset != 0) {
			if (polygon != null) {
				polygon.translate(0, yOffset);
				renderPolygon(g, polygon);
			} else {
				renderCircle(g, xDraw, yDraw + yOffset, diameter);
			}
		}
		
		// Draw x and y wrapped
		if (xOffset != 0 && yOffset != 0) {
			if (polygon != null) {
				polygon.translate(xOffset, 0);
				renderPolygon(g, polygon);
			} else {
				renderCircle(g, xDraw + xOffset, yDraw + yOffset, diameter);
			}
		}
	}
	
	private static void drawPolygon(Graphics g, IPolygon polygonObj, long extrapolateTime) {
		// Get the spaceship outline as a polygon
		Polygon polygon = polygonObj.getOutline(extrapolateTime);
		
		// Fill and draw outline
		g.fillPolygon(polygon);
		g.drawPolygon(polygon);
	}
	
	private static void renderCircle(Graphics g, int x, int y, int d) {
		g.fillOval(x, y, d, d);
		g.drawOval(x, y, d, d);
	}
	
	private static void renderPolygon(Graphics g, Polygon polygon) {
		g.fillPolygon(polygon);
		g.drawPolygon(polygon);
	}
	
	/**
	 * Draws neutral score markers with arbitrary text, visible to both players.
	 * Score marker positions are defined in pixels, not physics units.
	 */
	private void drawTextMarker(Graphics g, String message, int yLine) {
		int xCenter = getGameWidthPixels() / 2;
		Vector2DLong position0Inverted = new Vector2DLong(xCenter, yLine),
				position1NonInverted = new Vector2DLong(xCenter, getGameHeightPhysics() - yLine);
		drawScoreMarker(g, new ScoreMarker(message, position0Inverted, null, true));
		drawScoreMarker(g, new ScoreMarker(message, position1NonInverted, null, false));
	}
	
	/**
	 * Draws score markers with arbitrary text for each player.
	 * Score marker positions are defined in pixels, not physics units.
	 * 
	 * @param g
	 * @param messages
	 * @param yLine
	 * @param owner
	 */
	private void drawTextMarker(Graphics g, String[] messages, int yLine, Player owner) {
		int xCenter = getGameWidthPixels() / 2;
		Vector2DLong position0Inverted = new Vector2DLong(xCenter, yLine),
				position1NonInverted = new Vector2DLong(xCenter, getGameHeightPixels() - yLine);
		drawScoreMarker(g, new ScoreMarker(messages[0], position0Inverted, owner, true));
		drawScoreMarker(g, new ScoreMarker(messages[1], position1NonInverted, owner, false));
	}
	
	private static void drawScoreMarker(Graphics g, ScoreMarker scoreMarker) {
		// Set the color
		g.setColor(getOwnerColor(scoreMarker));
		
		// Center the text
		int xOffset = g.getFontMetrics().stringWidth(scoreMarker.value) / 2,
				yOffset = 5;
		int xDraw = (int)(scoreMarker.position.x + (scoreMarker.isInverted() ? xOffset : -xOffset)),
				yDraw = (int)(scoreMarker.position.y - (scoreMarker.isInverted() ? yOffset : -yOffset));
		
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
