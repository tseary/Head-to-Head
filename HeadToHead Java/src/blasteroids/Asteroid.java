package blasteroids;

import geometry.Vector2D;
import geometry.Vector2DLong;
import headtohead.IScorable;
import physics.PhysicsConstants;
import physics.PhysicsObject;

public class Asteroid extends PhysicsObject implements IScorable {
	protected int size;
	
	// Physics constants
	private final static double splitSpeedFactor = 1.8d;
	private final static double splitSeparateSpeed = PhysicsConstants.velocity(10d);
	private final static double bulletRelativeMass = 0.1d;
	
	private static final int SIZES = 5;
	
	private final static long[] radii = new long[SIZES];
	private final static double[] masses = new double[SIZES];
	private final static int[] scores = new int[SIZES];
	
	static {
		for (int i = 0; i < SIZES; i++) {
			double radius = 5.5d * Math.pow(2d, i);
			radii[i] = PhysicsConstants.distance(radius);
			masses[i] = Math.pow(radius, 2d);
			scores[i] = (int)(100 * Math.pow(1.5d, 2 - i));
		}
	}
	
	public Asteroid() {
		this(2);
	}
	
	public Asteroid(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Asteroid size must be non-negative.");
		}
		this.size = Math.min(size, SIZES - 1);
	}
	
	/**
	 * Creates a new Asteroid with shallow copies of this asteroid's velocity
	 * and acceleration vectors.
	 * 
	 * @return
	 */
	public Asteroid shallowClone() {
		Asteroid clone = new Asteroid(this.size);
		clone.position = this.position.clone();
		clone.velocity = this.velocity;
		clone.acceleration = this.acceleration;
		return clone;
	}
	
	// TODO Turn this into a function in PhysicsObject
	public void bounceWrapped(PhysicsObject object2, long width, long height) {
		// Unwrap
		unwrapPositions(this, object2, width, height);
		
		// Push the objects away from each other to prevent tangling
		Vector2DLong relativePosition = this.position.difference(object2.position);
		long radiusSum = this.getRadius() + object2.getRadius();
		double overlap = radiusSum - relativePosition.length();
		if (overlap > 0d) {
			final double minDistance = PhysicsConstants.distance(0.1d);
			relativePosition.setLength(0.5d * overlap + minDistance);
			this.position.add(relativePosition);
			object2.position.subtract(relativePosition);
		}
		
		Vector2D relativeVelocity = this.velocity.difference(object2.velocity);
		double velocityAway = relativeVelocity.dotProduct(relativePosition);
		System.out.println("velocityAway =\t" + velocityAway + "\t" + overlap);
		if (velocityAway > 0d) return;
		
		// Do elastic collision
		// TODO Make this more efficient
		Vector2D obj1VelocityFinal = getVelocity1Final(this, object2);
		Vector2D obj2VelocityFinal = getVelocity1Final(object2, this);
		
		this.velocity = obj1VelocityFinal;
		object2.velocity = obj2VelocityFinal;
		
		// Undo the effect of wrapping
		this.wrapPosition(width, height);
		object2.wrapPosition(width, height);
	}
	
	// TODO Turn this into a function in PhysicsObject
	private static Vector2D getVelocity1Final(PhysicsObject obj1, PhysicsObject obj2) {
		double mass1 = obj1.getMass(),
				mass2 = obj2.getMass();
		
		double massTerm = 2d * mass2 / (mass1 + mass2);
		
		Vector2DLong position2Minus1 = obj1.position.difference(obj2.position);
		
		double numerator = obj1.velocity.difference(obj2.velocity).dotProduct(position2Minus1);
		double denominator = position2Minus1.lengthSquared();
		
		return obj1.velocity.difference(position2Minus1.scalarProduct(massTerm * numerator / denominator));
	}
	
	public Asteroid split(Bullet bullet) {
		// Return null if this asteroid is destroyed by splitting
		if (size == 0) {
			return null;
		}
		
		// Reduce size and create a new asteroid of the same size
		this.size--;
		Asteroid otherHalf = new Asteroid(this.size);
		
		// A unit vector in the direction of the bullet impact
		Vector2D impactUnit = position.difference(bullet.position).toUnit();
		
		// Move this and the new asteroid away from each other
		Vector2D positionChangeUnit = new Vector2D(-impactUnit.y, impactUnit.x);
		Vector2DLong positionChange = new Vector2DLong(positionChangeUnit.scalarProduct(getRadius()));
		otherHalf.position = this.position.difference(positionChange);
		this.position.add(positionChange);
		
		// Speed up to make the tiny asteroids more dangerous
		this.velocity = this.velocity.scalarProduct(splitSpeedFactor);
		
		// The bullet velocity has an effect on both asteroid velocities
		this.velocity.add(bullet.velocity.scalarProduct(bulletRelativeMass));
		
		Vector2D velocityChange = positionChangeUnit.scalarProduct(splitSeparateSpeed);
		otherHalf.velocity = this.velocity.difference(velocityChange);
		this.velocity.add(velocityChange);
		
		return otherHalf;
	}
	
	@Override
	public long getRadius() {
		return radii[size];
	}
	
	@Override
	public double getMass() {
		return masses[size];
	}
	
	@Override
	public int getScore() {
		return scores[size];
	}
}
