package blasteroids;

import headtohead.Player;

public class Bullet extends AgedPhysicsObject implements IOwnable {
	
	private Player owner;
	
	public Bullet(Player owner) {
		this.owner = owner;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	@Override
	public double getRadius() {
		return 1d;
	}
}
