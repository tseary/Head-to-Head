package pong;

import java.awt.Color;
import java.awt.Graphics;

import headtohead.ArcadeButton;
import headtohead.HeadToHeadGameCanvas;
import headtohead.Player;

public class PongGamePanel extends HeadToHeadGameCanvas {
	private static final long serialVersionUID = 1L;
	
	// Paddles
	private Paddle[] paddles;
	final int paddleWidth = 25;
	final int paddleHeight = 4;
	final int paddleSpeed = 4;
	
	// Ball
	final int ballSize = 9;
	private int xBall = 0, yBall = 0,
			vxBall = 0, vyBall = 0;
	
	// Serve
	int server = 0;
	boolean ballOnPaddle = false;
	
	public PongGamePanel() {
		super(160, 30);
		
		// Create the paddles
		paddles = new Paddle[2];
		paddles[0] = new Paddle(0, 20, paddleWidth, paddleHeight);
		paddles[1] = new Paddle(0, getGameHeight() - 20 - paddleHeight, paddleWidth, paddleHeight);
	}
	
	@Override
	public void newGame() {
		// Center the paddles
		for (int i = 0; i < paddles.length; i++) {
			paddles[i].x = (getGameWidth() - paddles[i].w) / 2;
		}
		
		newRound();
		
		for (Player player : players) {
			player.score = 0;
		}
		
		gameTimer.start();
	}
	
	@Override
	public void newRound() {
		// Put the ball on the server's paddle
		putBallOnServerPaddle();
		players[server].getButton(1).resetPressCounter();
		
		// Center the ball
		// xBall = (gameWidth - ballSize) / 2;
		// yBall = (gameHeight - ballSize) / 2;
		
		// DEBUG
		// Start the ball moving
		// Random random = new Random();
		// vxBall = (random.nextBoolean() ? 1 : -1) * (random.nextInt(3) + 1);
		// vyBall = (random.nextBoolean() ? 1 : -1) * (random.nextInt(3) + 2);
	}
	
	@Override
	public void roundOver() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void gameOver() {
		// TODO Auto-generated method stub
	}
	
	private void putBallOnServerPaddle() {
		xBall = paddles[server].x + (paddles[server].w - ballSize) / 2;
		if (server == 0) {
			yBall = paddles[0].h + paddles[0].h;
		} else {
			yBall = paddles[1].y - ballSize;
		}
		ballOnPaddle = true;
	}
	
	@Override
	protected void gameTick() {
		// DEBUG
		// super.gameTick();
		
		// Move the paddles
		for (int i = 0; i < players.length; i++) {
			boolean leftPressed = players[i].getButton(0).isPressed(),
					rightPressed = players[i].getButton(2).isPressed();
			
			if (leftPressed ^ rightPressed) {
				paddles[i].x += rightPressed ? paddleSpeed : -paddleSpeed;
			}
			
			// Stay on screen
			paddles[i].x = Math.max(0,
					Math.min(paddles[i].x, getGameWidth() - paddles[i].w));
		}
		
		// Move the ball
		if (ballOnPaddle) {
			putBallOnServerPaddle();
		} else {
			xBall += vxBall;
			yBall += vyBall;
		}
		
		// Serve
		if (ballOnPaddle) {
			ArcadeButton serveButton = players[server].getButton(1);
			if (serveButton.getPressCounter() > 0) {
				ballOnPaddle = false;
				
				// Set ball x speed
				boolean leftPressed = players[server].getButton(0).isPressed(),
						rightPressed = players[server].getButton(2).isPressed();
				vxBall = (leftPressed ? -paddleSpeed : 0)
						+ (rightPressed ? paddleSpeed : 0);
				
				// Set ball y speed
				int direction = server == 0 ? 1 : -1;
				vyBall = direction * (serveButton.getPressCounter() + 1);
			}
		}
		
		// Bounce
		if (xBall <= 0) {
			xBall = 0;
			vxBall = Math.abs(vxBall);
		} else if ((xBall + ballSize) >= getGameWidth()) {
			xBall = getGameWidth() - ballSize;
			vxBall = -Math.abs(vxBall);
		}
		
		// Collision detection
		
		int yOverlap = paddles[0].y + paddles[0].h - yBall;
		int leftOverlap = xBall + ballSize - paddles[0].x;
		int rightOverlap = paddles[0].x + paddles[0].w - xBall;
		
		if (yOverlap >= 0) {
			// Ball might be touching top paddle
			
			// TODO Make functions for these
			final boolean centerHit = leftOverlap >= ballSize && rightOverlap >= ballSize;
			final boolean leftHit = leftOverlap >= 0 && leftOverlap < ballSize;
			final boolean rightHit = rightOverlap >= 0 && rightOverlap < ballSize;
			
			// Bounce vertically off top paddle
			if (leftHit || centerHit || rightHit) {
				yBall = paddles[0].y + paddles[0].h;
				vyBall = Math.abs(vyBall);
				
				// Randomly change x speed
				// TODO Apply spin if paddle is moving
				vxBall += (int) System.currentTimeMillis() % 3 - 1;
			} else if (yBall < paddles[0].y + paddles[0].h) {
				// Doomed
			}
			
			// Bounce off left side of top paddle
			if (vxBall > 0 && leftHit) {
				xBall = paddles[0].x - ballSize;
				vxBall = -Math.abs(vxBall);
			}
			
			// Bounce off right side of top paddle
			if (vxBall < 0 && rightHit) {
				xBall = paddles[0].x + paddles[0].w;
				vxBall = Math.abs(vxBall);
			}
			
		} else if (yBall + ballSize >= paddles[1].y) {
			// Ball might be touching bottom paddle
			if (xBall + ballSize >= paddles[1].x
					&& xBall <= paddles[1].x + paddles[1].w) {
				// Bounce off bottom paddle
				// TODO Make corner-to-corner bounce reverse vxBall
				yBall = paddles[1].y - ballSize;
				vyBall = -Math.abs(vyBall);
				
				// Randomly change x speed
				vxBall += (int) System.currentTimeMillis() % 3 - 1;
			}
		}
		
		// Point
		boolean player0Lost = yBall <= 0;
		boolean player1Lost = yBall + ballSize >= getGameHeight();
		if (player0Lost) {
			server = 1;
			players[1].score++;
			yBall = 0;
		} else if (player1Lost) {
			server = 0;
			players[0].score++;
			yBall = getGameHeight() - ballSize;
		}
		if (player0Lost || player1Lost) {
			// vxBall = 0;
			// vyBall = 0;
			newRound();
		}
	}
	
	@Override
	public void drawVideoFrame(Graphics g) {
		// Clear the frame (transparent)
		g.setColor(Color.DARK_GRAY.darker().darker());
		g.fillRect(0, 0, getGameWidth(), getGameHeight());
		
		// Draw the paddles
		for (int i = 0; i < players.length; i++) {
			g.setColor(players[i].getColor());
			// g.drawLine(paddles[i].x, paddles[i].y, paddles[i].x +
			// paddles[i].w,
			// paddles[i].y);
			g.fillRect(paddles[i].x, paddles[i].y, paddles[i].w, paddles[i].h);
			
			// Draw scores
			g.drawString(String.valueOf(players[i].score), 4,
					paddles[i].y - 4 + (i >= 1 ? paddles[i - 1].y + 1 : 0));
		}
		
		// Draw the ball
		g.setColor(Color.WHITE);
		g.fillRect(xBall, yBall, ballSize, ballSize);
	}
	
	@Override
	protected int[] getLeftHandedButtonOrder() {
		return null;
	}
}
