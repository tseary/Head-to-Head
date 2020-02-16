package tankbattle;

import geometry.Vector2D;
import physics.RotatableRectanglePhysicsObject;

public class Wall extends RotatableRectanglePhysicsObject {
	
	Vector2D[] outlineVectorsRelative;
	
	public Wall() {
		super(80d, 20d);
	}
	
	public boolean isInside(Vector2D point) {
		return false;
	}
}
