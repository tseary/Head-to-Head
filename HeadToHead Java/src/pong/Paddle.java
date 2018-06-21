package pong;

public class Paddle {
	public int x, y, w, h;
	
	int lastMove;
	
	public Paddle(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	public void move(int xChange) {
		x = xChange;
		lastMove = xChange;
	}
	
	public boolean isMoving() {
		return lastMove != 0;
	}
	
	public boolean isMovingLeft() {
		return lastMove < 0;
	}
	
	public boolean isMovingRight() {
		return lastMove > 0;
	}
}
