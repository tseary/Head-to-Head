package blasteroids;

import headtohead.IOwnable;
import headtohead.Player;
import physics.AgedPhysicsObject;

public class Bullet extends AgedPhysicsObject implements IOwnable {
	
	private Player owner;
	
	private static double radius = 1d;
	
	public Bullet(Player owner) {
		this.owner = owner;
	}
	
	public Player getOwner() {
		return owner;
	}
	
	@Override
	public double getRadius() {
		return radius;
	}
	
	@Override
	public double getMass() {
		return 1d;
	}
	
	public static void setRadius(double radius) {
		Bullet.radius = radius;
	}
}
