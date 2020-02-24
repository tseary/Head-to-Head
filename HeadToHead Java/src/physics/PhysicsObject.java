package physics;

import geometry.Vector2D;
import geometry.Vector2DLong;

public abstract class PhysicsObject {
	public Vector2DLong position;
	public Vector2D velocity, acceleration;
	
	public PhysicsObject() {
		position = new Vector2DLong();
		velocity = new Vector2D();
		acceleration = new Vector2D();
	}
	
	public PhysicsObject(PhysicsObject obj) {
		this.position = obj.position.clone();
		this.velocity = obj.velocity.clone();
		this.acceleration = obj.acceleration.clone();
	}
	
	abstract public long getRadius();
	
	abstract public double getMass();
	
	public void move(long deltaTime) {
		position.add(velocity.sum(acceleration.scalarProduct(deltaTime / 2d))
				.scalarProduct(deltaTime));
		velocity.add(acceleration.scalarProduct(deltaTime));
	}
	
	public boolean isTouchingWrapped(PhysicsObject obj, long width, long height) {
		// Unwrap
		unwrapPositions(this, obj, width, height);
		
		// Calculate
		boolean touching = isTouching(obj);
		
		// Undo the effect of wrapping
		this.wrapPosition(width, height);
		obj.wrapPosition(width, height);
		
		return touching;
	}
	
	/**
	 * If objA and objB are near opposite edges of the universe, unwraps the
	 * distant one to be at a negative position closer to the origin.
	 * 
	 * @param obj
	 * @param width
	 * @param height
	 * @return
	 */
	protected static void unwrapPositions(PhysicsObject objA, PhysicsObject objB, long width, long height) {
		// Check if we need to unwrap in x
		long xDistance = objA.position.x - objB.position.x;	// positive if A is on the right
		boolean unwrapX = Math.abs(xDistance) > 0.5d * width;
		
		// Unwrap the objects in x
		if (unwrapX) {
			boolean unwrapAX = xDistance > 0;
			if (unwrapAX) {
				objA.position.x -= width;
			} else {
				objB.position.x -= width;
			}
		}
		
		// Check if we need to unwrap in y
		long yDistance = objA.position.y - objB.position.y;
		boolean unwrapY = Math.abs(yDistance) > height / 2;
		
		// Unwrap the objects in y
		if (unwrapY) {
			boolean unwrapAY = yDistance > 0;
			if (unwrapAY) {
				objA.position.y -= height;
			} else {
				objB.position.y -= height;
			}
		}
	}
	
	public void wrapPosition(long width, long height) {
		position.x %= width;
		if (position.x < 0) {
			position.x += width;
		}
		position.y %= height;
		if (position.y < 0) {
			position.y += height;
		}
	}
	
	public boolean isTouching(PhysicsObject obj) {
		// Do AABB collision first
		if (!isTouchingAABB(obj)) return false;
		
		// Do circle collision
		return isTouchingCircular(obj);
	}
	
	protected boolean isTouchingAABB(PhysicsObject obj) {
		long radiusSum = getRadius() + obj.getRadius();
		return Math.abs(position.x - obj.position.x) <= radiusSum &&
				Math.abs(position.y - obj.position.y) <= radiusSum;
	}
	
	protected boolean isTouchingCircular(PhysicsObject obj) {
		long radiusSum = getRadius() + obj.getRadius();
		return distanceSquaredTo(obj) <= radiusSum * radiusSum;
	}
	
	public long distanceSquaredTo(PhysicsObject obj) {
		return this.position.difference(obj.position).lengthSquared();
	}
}
