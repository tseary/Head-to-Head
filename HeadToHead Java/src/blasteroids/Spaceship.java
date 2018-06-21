package blasteroids;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import geometry.Vector2D;
import headtohead.IScorable;
import headtohead.Player;

public class Spaceship extends RotatablePhysicsObject implements IOwnable, IPolygon, IScorable {
	
	private Player owner;
	
	static final double bulletSpeed = 50d;
	
	// private boolean alive = true;
	private static final int fullHealth = 3;
	private int health = fullHealth;
	
	/**
	 * 
	 * @param heading
	 *            The heading angle in radians.
	 */
	public Spaceship(double angle, Player owner) {
		this.angle = angle;
		this.owner = owner;
	}
	
	/**
	 * Gets a bullet moving at this spaceship's heading angle relative to this
	 * spaceship.
	 * 
	 * @return
	 */
	public Bullet shoot() {
		Bullet bullet = new Bullet(owner);
		bullet.position = this.position
				.sum(new Vector2D(getRadius() * 1.3d, angle, true));
		bullet.velocity = this.velocity
				.sum(new Vector2D(bulletSpeed, angle, true));
		return bullet;
	}
	
	public void takeHit() {
		// Lose health
		health--;
		
		// Getting hit slows you down
		// velocity = new Vector2D(0d, 0d);
		velocity = velocity.scalarProduct(0.5d);
	}
	
	public void setAlive(boolean alive) {
		health = alive ? fullHealth : 0;
	}
	
	public int getHealth() {
		return health;
	}
	
	public boolean isAlive() {
		return health > 0;
	}
	
	@Override
	public boolean isTouching(PhysicsObject obj) {
		// Check if any point on the outline is inside the obj
		Vector2D[] outline = getOutlineVectors();
		double objRadiusSqr = Math.pow(obj.getRadius(), 2d);
		for (Vector2D point : outline) {
			if (point.difference(obj.position).lengthSquared() <= objRadiusSqr) {
				return true;
			}
		}
		
		// If the outline isn't touching, do the base collision detection
		return super.isTouching(obj);
	}
	
	@Override
	public Vector2D[] getOutlineVectors() {
		// A number of radians < PI
		final double wingAngle = 2.4d;
		
		// The scale of the triangle
		final double vertexRadius = getRadius() / 0.7d;
		
		return new Vector2D[] {
				position.sum(new Vector2D(vertexRadius, angle, true)),
				position.sum(new Vector2D(vertexRadius, angle + wingAngle, true)),
				position.sum(new Vector2D(vertexRadius, angle - wingAngle, true)) };
	}
	
	@Override
	public Polygon getOutline() {
		return IPolygon.vectorsToPolygon(getOutlineVectors());
	}
	
	/**
	 * Gets the pieces that this ship would break into if it just died.
	 * The ship is broken from its center to the midpoint of each edge.
	 * @return
	 */
	public Collection<Fragment> getFragments() {
		// Create arrays
		Vector2D[] shipOutline = getOutlineVectors();
		Vector2D[] shipOutlineMidpoints = new Vector2D[shipOutline.length];
		Collection<Fragment> fragments = new ArrayList<Fragment>(shipOutline.length);
		
		// Calculate midpoints
		for (int i = 0; i < shipOutlineMidpoints.length; i++) {
			shipOutlineMidpoints[i] = shipOutline[i].sum(
					shipOutline[(i + 1) % shipOutline.length]).scalarProduct(0.5d);
		}
		
		// The proportional speed at which fragments move away from the ship center
		final double fragmentSplitSpeedMax = 5d;
		final double fragmentRotationSpeedMax = 6d;
		
		// Create fragments
		Random random = new Random();
		for (int i = 0; i < shipOutline.length; i++) {
			Fragment fragment = new Fragment(new Vector2D[] {
					this.position,
					shipOutlineMidpoints[i],
					shipOutline[i],
					shipOutlineMidpoints[i > 0 ? i - 1 : ((shipOutlineMidpoints.length - 1) % shipOutlineMidpoints.length)] },
					this.owner);
			
			fragment.velocity = this.velocity.sum(
					shipOutline[i].difference(this.position).scalarProduct(
							fragmentSplitSpeedMax * random.nextDouble()));
			
			fragment.angularVelocity = fragmentRotationSpeedMax * (2d * random.nextDouble() - 1d);
			
			fragments.add(fragment);
		}
		
		return fragments;
	}
	
	@Override
	public double getRadius() {
		return 8d;
	}
	
	@Override
	public Player getOwner() {
		return owner;
	}
	
	@Override
	public int getScore() {
		return (int) (10d * velocity.length());
	}
}
