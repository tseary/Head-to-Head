package tankbattle;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import blasteroids.Bullet;
import blasteroids.Fragment;
import geometry.Vector2D;
import headtohead.DebugMode;
import headtohead.IOwnable;
import headtohead.IScorable;
import headtohead.Player;
import physics.IPolygon;
import physics.RotatablePolygonPhysicsObject;

public class Tank extends RotatablePolygonPhysicsObject implements IOwnable, IPolygon, IScorable {
	
	private Player owner;
	
	static final double bulletSpeed = 100d;
	
	private static final int fullHealth = 3;
	private int health = fullHealth;
	
	private static final int fullAmmo = 15;
	private int ammo = fullAmmo;
	
	/**
	 * 
	 * @param heading
	 *            The heading angle in radians.
	 */
	public Tank(double angle, Player owner) {
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
		// Can't shoot if out of ammo
		if (ammo <= 0) {
			return null;
		}
		
		ammo--;
		
		// Unlimited ammo in debug mode
		if (DebugMode.isEnabled() && ammo == 0) ammo = 1;
		
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
	
	public int getAmmo() {
		return ammo;
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
	public Vector2D[] getOutlineVectors(double extrapolateTime) {
		final double cornerAngle1 = 0.165d * Math.PI;
		final double cornerAngle2 = 0.138d * Math.PI;
		final double cornerAngle3 = 0.20d * Math.PI;
		
		final double cornerRadius1 = 1.06d * getRadius();
		final double cornerRadius2 = 1.24d * getRadius();
		final double cornerRadius3 = 1.39d * getRadius();
		
		final double barrelBaseAngle = 0.025d * Math.PI;
		final double barrelEndAngle = 0.015d * Math.PI;
		
		final double barrelBaseRadius = 0.93d * getRadius();
		final double barrelEndRadius = 1.60d * getRadius();
		
		Vector2D outlinePosition = IPolygon.extrapolatePosition(this, extrapolateTime);
		
		return new Vector2D[] {
				outlinePosition.sum(new Vector2D(barrelBaseRadius, angle - barrelBaseAngle, true)),	// Barrel
				outlinePosition.sum(new Vector2D(barrelEndRadius, angle - barrelEndAngle, true)),
				outlinePosition.sum(new Vector2D(barrelEndRadius, angle + barrelEndAngle, true)),
				outlinePosition.sum(new Vector2D(barrelBaseRadius, angle + barrelBaseAngle, true)),
				
				outlinePosition.sum(new Vector2D(cornerRadius1, angle + cornerAngle1, true)),	// Front left
				outlinePosition.sum(new Vector2D(cornerRadius2, angle + cornerAngle2, true)),
				outlinePosition.sum(new Vector2D(cornerRadius3, angle + cornerAngle3, true)),
				
				outlinePosition.sum(new Vector2D(cornerRadius3, angle + Math.PI - cornerAngle3, true)),	// Rear left
				outlinePosition.sum(new Vector2D(cornerRadius2, angle + Math.PI - cornerAngle2, true)),
				outlinePosition.sum(new Vector2D(cornerRadius1, angle + Math.PI - cornerAngle1, true)),
				
				outlinePosition.sum(new Vector2D(cornerRadius1, angle + Math.PI + cornerAngle1, true)),	// Rear right
				outlinePosition.sum(new Vector2D(cornerRadius2, angle + Math.PI + cornerAngle2, true)),
				outlinePosition.sum(new Vector2D(cornerRadius3, angle + Math.PI + cornerAngle3, true)),
				
				outlinePosition.sum(new Vector2D(cornerRadius3, angle - cornerAngle3, true)),	// Front right
				outlinePosition.sum(new Vector2D(cornerRadius2, angle - cornerAngle2, true)),
				outlinePosition.sum(new Vector2D(cornerRadius1, angle - cornerAngle1, true)) };
	}
	
	@Override
	public Polygon getOutline(double extrapolateTime) {
		return IPolygon.vectorsToPolygon(getOutlineVectors(extrapolateTime));
	}
	
	/**
	 * Gets the pieces that this ship would break into if it just died.
	 * The ship is broken from its center to the midpoint of each edge.
	 * @return
	 */
	public Collection<Fragment> getFragments() {
		// Create arrays
		Vector2D[] shipOutline = getOutlineVectors(0d);
		Vector2D[] shipOutlineMidpoints = new Vector2D[shipOutline.length];
		Collection<Fragment> fragments = new ArrayList<Fragment>(shipOutline.length + 1);
		
		// The proportional speed at which fragments move away from the ship center
		final double fragmentSplitSpeedMax = 10d;
		final double fragmentRotationSpeedMax = 20d;
		
		Random random = new Random();
		
		// Create a little man-shaped fragment
		double headAngle = this.angle + Math.PI;
		Fragment fragmentMan = new Fragment(new Vector2D[] {
				this.position,	// Shoulder
				this.position.sum(new Vector2D(2d, headAngle, true)),	// Head
				this.position,	// Shoulder
				this.position.sum(new Vector2D(3.5d, headAngle + 0.45d * Math.PI, true)),	// Hand
				this.position.sum(new Vector2D(0.3d, headAngle + 0.6d * Math.PI, true)),	// Armpit
				this.position.sum(new Vector2D(5d, headAngle + 0.9d * Math.PI, true)),		// Leg
				this.position.sum(new Vector2D(1.5d, headAngle + Math.PI, true)),			// Crotch
				this.position.sum(new Vector2D(5d, headAngle + 1.1d * Math.PI, true)),		// Leg
				this.position.sum(new Vector2D(0.3d, headAngle + 1.4d * Math.PI, true)),	// Armpit
				this.position.sum(new Vector2D(3.5d, headAngle + 1.55d * Math.PI, true)) },	// Hand
				null);
		
		fragmentMan.velocity = this.velocity.scalarProduct(0.3d);
		// fragmentMan.angularVelocity = fragmentRotationSpeedMax * (2d * random.nextDouble() - 1d);
		
		fragments.add(fragmentMan);
		
		// Calculate midpoints
		for (int i = 0; i < shipOutlineMidpoints.length; i++) {
			shipOutlineMidpoints[i] = shipOutline[i].sum(
					shipOutline[(i + 1) % shipOutline.length]).scalarProduct(0.5d);
		}
		
		// Create fragments
		for (int i = 0; i < shipOutline.length; i++) {
			Fragment fragment = new Fragment(new Vector2D[] {
					this.position,
					shipOutlineMidpoints[i],
					shipOutline[i],
					shipOutlineMidpoints[i > 0
							? i - 1 : ((shipOutlineMidpoints.length - 1) % shipOutlineMidpoints.length)] },
					this.owner);
			
			fragment.velocity = this.velocity.sum(
					shipOutline[i].difference(this.position).scalarProduct(
							fragmentSplitSpeedMax * random.nextDouble()));
			fragment.acceleration = fragment.velocity.scalarProduct(-2d);
			fragment.angularVelocity = fragmentRotationSpeedMax * (2d * random.nextDouble() - 1d);
			fragment.angularAcceleration = fragment.angularVelocity * -2d;
			
			fragments.add(fragment);
		}
		
		return fragments;
	}
	
	@Override
	public double getRadius() {
		return 12d;
	}
	
	@Override
	public Player getOwner() {
		return owner;
	}
	
	@Override
	public int getScore() {
		return 100 * (fullHealth - health);
	}
}
