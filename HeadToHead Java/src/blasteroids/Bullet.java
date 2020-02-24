package blasteroids;

import headtohead.IOwnable;
import headtohead.Player;
import physics.AgedPhysicsObject;
import physics.PhysicsConstants;

public class Bullet extends AgedPhysicsObject implements IOwnable {
	
	private Player owner;
	
	private static long radius = PhysicsConstants.distance(1d);
	
	public Bullet(Player owner) {
		this.owner = owner;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	@Override
	public long getRadius() {
		return radius;
	}
	
	@Override
	public double getMass() {
		return 1d;
	}
	
	public static void setRadius(long radius) {
		Bullet.radius = radius;
	}
}
