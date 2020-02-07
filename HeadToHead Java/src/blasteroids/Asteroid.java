package blasteroids;

import geometry.Vector2D;
import headtohead.IScorable;

public class Asteroid extends PhysicsObject implements IScorable {
	protected int size;
	
	// Physics constants
	private final static double splitSpeedFactor = 1.8d;
	private final static double splitSeparateSpeed = 10d;
	private final static double bulletRelativeMass = 0.1d;
	
	private final static double[] radii = new double[] { 5.5d, 11d, 22d, 44d, 88d };
	private final static int[] scores = new int[] { 225, 150, 100, 66, 44 };
	
	public Asteroid() {
		this(2);
	}
	
	public Asteroid(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Asteroid size must be non-negative.");
		}
		this.size = size;
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
	
	// TODO Rename asteroidB
	public void bounceWrapped(PhysicsObject asteroidB, double width, double height) {
		// Unwrap
		unwrapPositions(this, asteroidB, width, height);
		
		// Push the asteroids away from each other to prevent tangling
		double radiusSum = this.getRadius() + asteroidB.getRadius();
		Vector2D relativePosition = this.position.difference(asteroidB.position);
		double overlap = radiusSum - relativePosition.length();
		if (overlap > 0d) {
			relativePosition.setLength(0.5d * overlap);
			this.position.add(relativePosition);
			asteroidB.position.subtract(relativePosition);
		}
		
		// Do elastic collision
		// TODO Make this more efficient
		Vector2D ast1VelocityFinal = getVelocity1Final(this, asteroidB);
		Vector2D ast2VelocityFinal = getVelocity1Final(asteroidB, this);
		
		this.velocity = ast1VelocityFinal;
		asteroidB.velocity = ast2VelocityFinal;
		
		// Undo the effect of wrapping
		this.wrapPosition(width, height);
		asteroidB.wrapPosition(width, height);
	}
	
	// TODO Turn this into a function in PhysicsObject
	private static Vector2D getVelocity1Final(PhysicsObject ast1, PhysicsObject ast2) {
		double mass1 = Math.pow(ast1.getRadius(), 2d),
				mass2 = Math.pow(ast2.getRadius(), 2d);
		
		double massTerm = 2d * mass2 / (mass1 + mass2);
		
		Vector2D position2Minus1 = ast1.position.difference(ast2.position);
		
		double numerator = ast1.velocity.difference(ast2.velocity).dotProduct(position2Minus1);
		double denominator = position2Minus1.lengthSquared();
		
		return ast1.velocity.difference(position2Minus1.scalarProduct(massTerm * numerator / denominator));
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
		Vector2D positionChange = positionChangeUnit.scalarProduct(getRadius());
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
	public double getRadius() {
		if (size < radii.length) {
			return radii[size];
		}
		return 5.5d * Math.pow(2d, size);
	}
	
	@Override
	public int getScore() {
		if (size < scores.length) {
			return scores[size];
		}
		return (int)(100 * Math.pow(1.5d, 2 - size));
	}
}
