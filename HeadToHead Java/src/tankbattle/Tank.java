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
import physics.RotatableRectanglePhysicsObject;

public class Tank extends RotatableRectanglePhysicsObject implements IOwnable, IScorable {
	
	private Player owner;
	
	static final double bulletSpeed = 150d;
	
	private static final int fullHealth = 3;
	private int health = fullHealth;
	
	private static final int fullAmmo = 15;
	private int ammo = fullAmmo;
	
	// Aestetic dimensions
	private static final double barrelWidth = 2d,
			barrelLength = 10d;
	
	/**
	 * 
	 * @param heading
	 *            The heading angle in radians.
	 */
	public Tank(double angle, Player owner) {
		super(30d, 20d);
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
		
		// Starting position is front of tank + 1.5 * bullet radius
		Bullet bullet = new Bullet(owner);
		Vector2D forwardUnit = new Vector2D(1d, angle, true);
		bullet.position = this.position.sum(forwardUnit.scalarProduct(
				size.x + 1.5d * bullet.getRadius()));
		bullet.velocity = this.velocity.sum(forwardUnit.scalarProduct(bulletSpeed));
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
	public Polygon getOutline(double extrapolateTime) {
		
		final Vector2D corner3 = size,
				corner2 = new Vector2D(corner3.x, 0.6d * corner3.y),
				corner1 = new Vector2D(0.85d * corner2.x, corner2.y);
		final Vector2D barrelBase = new Vector2D(corner1.x, barrelWidth / 2d);
		final Vector2D barrelEnd = new Vector2D(barrelBase.x + barrelLength, barrelBase.y);
		
		final double cornerAngle1 = corner1.angle();
		final double cornerAngle2 = corner2.angle();
		final double cornerAngle3 = corner3.angle();
		
		final double cornerRadius1 = corner1.length();
		final double cornerRadius2 = corner2.length();
		final double cornerRadius3 = corner3.length();
		
		final double barrelBaseAngle = barrelBase.angle();
		final double barrelEndAngle = barrelEnd.angle();
		
		final double barrelBaseRadius = barrelBase.length();
		final double barrelEndRadius = barrelEnd.length();
		
		// TODO Clean up this redundant garbage
		
		Vector2D outlinePosition = IPolygon.extrapolatePosition(this, extrapolateTime);
		
		Vector2D[] outlineVectors = new Vector2D[] {
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
		
		return IPolygon.vectorsToPolygon(outlineVectors);
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
	public Player getOwner() {
		return owner;
	}
	
	@Override
	public int getScore() {
		return 100 * (fullHealth - health);
	}
}
