package blasteroids;

import headtohead.IOwnable;
import headtohead.Player;
import physics.AgedPhysicsObject;

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
	
	@Override
	public double getMass() {
		return 1d;
	}
}
