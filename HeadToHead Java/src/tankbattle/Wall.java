package tankbattle;

import geometry.Vector2D;
import physics.RotatableRectanglePhysicsObject;

public class Wall extends RotatableRectanglePhysicsObject {
	
	Vector2D[] outlineVectorsRelative;
	
	public Wall() {
		this(100d);
	}
	
	public Wall(double length) {
		this(length, 10d);
	}
	
	public Wall(double length, double width) {
		super(length, width);
	}
	
	public boolean isInside(Vector2D point) {
		return false;
	}
}
