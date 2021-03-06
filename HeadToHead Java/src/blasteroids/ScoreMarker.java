package blasteroids;

import geometry.Vector2DLong;
import headtohead.IOwnable;
import headtohead.Player;
import physics.AgedPhysicsObject;

public class ScoreMarker extends AgedPhysicsObject implements IOwnable {
	
	public String value;
	
	private Player owner;
	
	private boolean inverted = false;
	
	public ScoreMarker(String value, Vector2DLong position, Player owner, boolean inverted) {
		this.value = value;
		this.position = position.clone();
		this.owner = owner;
		this.inverted = inverted;
	}
	
	@Override
	public void move(long deltaTime) {
		age += deltaTime;
	}
	
	@Override
	public long getRadius() {
		return 0;
	}
	
	@Override
	public double getMass() {
		return 0d;
	}
	
	@Override
	public Player getOwner() {
		return owner;
	}
	
	public boolean isInverted() {
		return inverted;
	}
}
