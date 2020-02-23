package blasteroids;

import headtohead.IOwnable;
import headtohead.Player;
import physics.AgedPhysicsObject;
import physics.PhysicsConstants;

public class Bullet extends AgedPhysicsObject implements IOwnable {
	
	private Player owner;
	
	public Bullet(Player owner) {
		this.owner = owner;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	@Override
	public long getRadius() {
		return PhysicsConstants.distance(1);
	}
	
	@Override
	public double getMass() {
		return 1d;
	}
}
