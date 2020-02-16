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
import geometry.SpaceVector2D;
import geometry.Vector2D;
import headtohead.HeadToHeadGameCanvas;
import headtohead.IOwnable;
import headtohead.IScorable;
import headtohead.Player;
import physics.IPolygon;
import physics.PhysicsObject;
import sound.SoundName;

// TODO Add trees as visual cover and walls (buildings) as physical cover

public class TankBattleGameCanvas extends HeadToHeadGameCanvas {
	private static final long serialVersionUID = 1L;
	
	// Physics constants
	private static final int gameTimerFPS = 60;
	private static final double tankMaxSpeed = 70d;
	private static final double tankThrust = 180d;
	private static final double tankDrag = 2d;
	private static final double tankSteeringAccel = 8d;
	private static final double tankSteeringDrag = 6d;
	private static final double bulletMaxAge = 3.333d;
	protected double deltaTimeAlive;
	protected double deltaTimeDead;
	
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
		
		Bullet.setRadius(2d);
		
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
			
			Vector2D position1 = new Vector2D(
					(i != 0 ? 0.07d : 0.93d) * getGameWidth(),
					(i != 0 ? 0.90d : 0.10d) * getGameHeight());
			playerScoreMarkers.add(new ScoreMarker(String.valueOf(0),
					position1, player, isPlayerInverted(player)));
			
			Vector2D position2 = new Vector2D(
					(i != 0 ? 0.93d : 0.07d) * getGameWidth(),
					(i != 0 ? 0.10d : 0.90d) * getGameHeight() + (i != 0 ? 15 : -15));
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
		deltaTimeAlive = getPhysicsTickMillis() / 1000d;
		deltaTimeDead = deltaTimeAlive / 4d;
	}
	
	@Override
	public void newRound() {
		// Create player ships
		if (players.length >= 1) {
			Tank player0Tank = new Tank(0.25d * 2d * Math.PI, players[0]);
			player0Tank.position.x = getGameWidth() / 2;
			player0Tank.position.y = getGameHeight() / 10;
			tanks[0] = player0Tank;
		}
		
		if (players.length >= 2) {
			Tank player1Tank = new Tank(0.75d * 2d * Math.PI, players[1]);
			player1Tank.position.x = getGameWidth() / 2;
			player1Tank.position.y = getGameHeight() * 9 / 10;
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
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
			// Choose a random wall postion, not to close to any tanks
			Vector2D wallPosition;
			double minDistanceSqrToTank;
			do {
				// Choose a random wall position
				wallPosition = new Vector2D(random.nextDouble() * getGameWidth(),
						random.nextDouble() * getGameHeight());
				
				// Calculate the distance to the closest tank
				minDistanceSqrToTank = Double.MAX_VALUE;
				for (Tank tank : tanks) {
					double distanceSqr = tank.position.difference(wallPosition).lengthSquared();
					minDistanceSqrToTank = Math.min(minDistanceSqrToTank, distanceSqr);
				}
			} while (minDistanceSqrToTank < 10000d);
			
			// Create the wall
			Wall wall = new Wall(80d);
			wall.position = wallPosition;
			
			// Rotate the wall randomly
			int wallAngle = random.nextInt(4);
			wall.angle = wallAngle * Math.PI / 4d;
			
			walls.add(wall);
		}
		
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
		
		collideTankToWall();
		collideTankToTank();
		collideBulletToWall();
		collideBulletToTank();
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
	
	private void moveEverything(double deltaTime) {
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
			
			tank.wrapPosition(getGameWidth(), getGameHeight());
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
			
			bullet.wrapPosition(getGameWidth(), getGameHeight());
		}
		
		// Move fragments
		for (Fragment fragment : fragments) {
			fragment.move(deltaTime);
		}
	}
	
	private void ageScoreMarkers(double deltaTime) {
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
				
				// Tank is touching wall
				// Janky "push off" implementation results in weird lack of friction
				// TODO Calculate normal force or something
				tank.position = wall.position.sum(
						tank.position.difference(wall.position).scalarProduct(1.05d));
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
					Vector2D positionDifference = tankA.position.difference(tankB.position);
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
					SpaceVector2D surfaceNormal = wall.getSurfaceNormal(bullet.position);
					
					// Reflect the velocity about the normal
					Vector2D v1 = bullet.velocity,
							v2 = surfaceNormal.vector;
					double k = v1.dotProduct(v2) / v2.dotProduct(v2);
					bullet.velocity.add(v2.scalarProduct(-2d * k));
					
					// Put the bullet on the surface of the wall
					bullet.position = surfaceNormal.position.sum(
							surfaceNormal.vector.scalarProduct(bullet.getRadius()));
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
		extrapolate *= (isRoundOver() ? deltaTimeDead : deltaTimeAlive);
		
		// Clear the frame (background colour)
		g.setColor(new Color(0x006000));
		g.fillRect(0, 0, getGameWidth(), getGameHeight());
		
		// Draw the tank fragments
		for (Fragment fragment : fragments) {
			g.setColor(getOwnerColor(fragment));
			drawPolygon(g, fragment, extrapolate);
		}
		
		// Draw the walls
		for (Wall wall : walls) {
			g.setColor(Color.LIGHT_GRAY);
			drawPolygon(g, wall, extrapolate);
		}
		
		// Draw the player tanks
		for (Tank tank : tanks) {
			if (!tank.isAlive()) {
				continue;
			}
			g.setColor(getOwnerColor(tank));
			drawPolygon(g, tank, extrapolate);
		}
		
		// DEBUG
		// Draw lines demonstarting surface normals
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
			drawPhysicsObject(g, bullet, extrapolate);
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
			
			int yBack = i == 0 ? (getGameHeight() / 10 - 6) : (getGameHeight() * 9 / 10 + 6);
			int yFront = yBack + (i == 0 ? rectHeight : -rectHeight);
			int xFirst = i == 0 ? 50 : (getGameWidth() - (50 + rectWidth) - 1);
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
			int xBar = i == 0 ? (59 - barWidth) : (getGameWidth() - 59);
			int yBar = i == 0 ? 65 : (getGameHeight() - 65 - barHeight);
			
			for (int h = 0; h < tanks[i].getHealth(); h++) {
				g.fillRect(xBar, yBar, barWidth, barHeight);
			}
		}
		
		// Draw text on top of everything
		final int yLine1 = getGameHeight() / 5;
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
	private void drawPhysicsObject(Graphics g, PhysicsObject obj, double extrapolateTime) {
		drawPhysicsObject(g, obj, extrapolateTime, false);
	}
	
	private void drawPhysicsObject(Graphics g, PhysicsObject obj, double extrapolateTime, boolean wrap) {
		
		Vector2D drawPosition = IPolygon.extrapolatePosition(obj, extrapolateTime);
		
		int radius = Math.max(1, (int)obj.getRadius());
		int xDraw = (int)drawPosition.x - radius;
		int yDraw = (int)drawPosition.y - radius;
		int diameter = 2 * radius;
		
		g.fillOval(xDraw, yDraw, diameter, diameter);
		g.drawOval(xDraw, yDraw, diameter, diameter);
		
		if (!wrap) {
			return;
		}
		
		int xOffset = 0, yOffset = 0;
		
		// Draw wrapped copies of the object
		boolean nearLeft = drawPosition.x < obj.getRadius(),
				nearRight = drawPosition.x > getGameWidth() - obj.getRadius();
		if (nearLeft) {
			xOffset = getGameWidth();
		} else if (nearRight) {
			xOffset = -getGameWidth();
		}
		
		boolean nearTop = drawPosition.y < obj.getRadius(),
				nearBottom = drawPosition.y > getGameHeight() - obj.getRadius();
		if (nearTop) {
			yOffset = getGameHeight();
		} else if (nearBottom) {
			yOffset = -getGameHeight();
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
	
	private static void drawPolygon(Graphics g, IPolygon polygonObj, double extrapolateTime) {
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
		int xCenter = getGameWidth() / 2;
		Vector2D position0Inverted = new Vector2D(xCenter, yLine);
		Vector2D position1NonInverted = new Vector2D(xCenter, getGameHeight() - yLine);
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
		int xCenter = getGameWidth() / 2;
		Vector2D position0Inverted = new Vector2D(xCenter, yLine);
		Vector2D position1NonInverted = new Vector2D(xCenter, getGameHeight() - yLine);
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
