package blasteroids;

import geometry.Vector2D;

public abstract class PhysicsObject {
	
	public Vector2D position, velocity, acceleration;
	
	public PhysicsObject() {
		position = new Vector2D(0d, 0d);
		velocity = new Vector2D(0d, 0d);
		acceleration = new Vector2D(0d, 0d);
	}
	
	public PhysicsObject(PhysicsObject obj) {
		this.position = obj.position.clone();
		this.velocity = obj.velocity.clone();
		this.acceleration = obj.acceleration.clone();
	}
	
	abstract public double getRadius();
	
	public void move(double deltaTime) {
		position.add(velocity.sum(acceleration.scalarProduct(deltaTime / 2d)).scalarProduct(deltaTime));
		velocity.add(acceleration.scalarProduct(deltaTime));
	}
	
	public boolean isTouchingWrapped(PhysicsObject obj, double width, double height) {
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
	protected static void unwrapPositions(PhysicsObject objA, PhysicsObject objB, double width, double height) {
		// Check if we need to unwrap in x
		double xLowThreshold = 0.25d * width;
		double xHighThreshold = 0.75d * width;
		boolean wrapObjBX = objA.position.x < xLowThreshold && objB.position.x > xHighThreshold,
				wrapObjAX = objB.position.x < xLowThreshold && objA.position.x > xHighThreshold;
		
		// Unwrap the objects in x
		if (wrapObjBX) {
			objB.position.x -= width;
		} else if (wrapObjAX) {
			objA.position.x -= width;
		}
		
		// Check if we need to unwrap in y
		double yLowThreshold = 0.25d * height;
		double yHighThreshold = 0.75d * height;
		boolean wrapObjBY = objA.position.y < yLowThreshold && objB.position.y > yHighThreshold,
				wrapObjAY = objB.position.y < yLowThreshold && objA.position.y > yHighThreshold;
		
		// Unwrap the objects in y
		if (wrapObjBY) {
			objB.position.y -= height;
		} else if (wrapObjAY) {
			objA.position.y -= height;
		}
	}
	
	protected void wrapPosition(double width, double height) {
		position.x %= width;
		if (position.x < 0d) {
			position.x += width;
		}
		position.y %= height;
		if (position.y < 0d) {
			position.y += height;
		}
	}
	
	public boolean isTouching(PhysicsObject obj) {
		double radiusSum = getRadius() + obj.getRadius();
		
		// Do AABB collision first
		if (Math.abs(position.x - obj.position.x) > radiusSum ||
				Math.abs(position.y - obj.position.y) > radiusSum) {
			return false;
		}
		
		// Do circle collision
		return distanceSquaredTo(obj) <= radiusSum * radiusSum;
	}
	
	public double distanceSquaredTo(PhysicsObject obj) {
		return this.position.difference(obj.position).lengthSquared();
	}
	
	public double getMass() {
		return 1d;
	}
}
