package tankbattle;

import geometry.Vector2D;
import physics.PhysicsConstants;
import physics.RotatableRectanglePhysicsObject;

public class Wall extends RotatableRectanglePhysicsObject {
	
	Vector2D[] outlineVectorsRelative;
	
	public Wall() {
		super(PhysicsConstants.distance(100), PhysicsConstants.distance(10));
	}
	
	public boolean isInside(Vector2D point) {
		return false;
	}
}
