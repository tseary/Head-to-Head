package tankbattle;

import geometry.Vector2D;
import physics.PhysicsConstants;
import physics.RotatableRectanglePhysicsObject;

public class Wall extends RotatableRectanglePhysicsObject {
	
	Vector2D[] outlineVectorsRelative;
	
	public Wall() {
		this(PhysicsConstants.distance(100));
	}
	
	public Wall(double length) {
		this(length, PhysicsConstants.distance(10));
	}
	
	public Wall(double length, double width) {
		super(length, width);
	}
	
	public boolean isInside(Vector2D point) {
		return false;
	}
}
