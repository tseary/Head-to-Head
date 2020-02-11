package tankbattle;

import geometry.Vector2D;
import physics.RotatablePolygonPhysicsObject;

public class Wall extends RotatablePolygonPhysicsObject {
	
	Vector2D[] outlineVectorsRelative;
	
	public Wall() {
		outlineVectorsRelative = new Vector2D[] {
				new Vector2D(20, 5),
				new Vector2D(20, -5),
				new Vector2D(-20, -5),
				new Vector2D(-20, 5)
		};
	}
	
	@Override
	public Vector2D[] getOutlineVectors(double extrapolateTime) {
		Vector2D[] outlineVectors = new Vector2D[outlineVectorsRelative.length];
		for (int i = 0; i < outlineVectors.length; i++) {
			outlineVectors[i] = outlineVectorsRelative[i].sum(position);
		}
		return outlineVectors;
	}
	
	@Override
	public double getRadius() {
		return outlineVectorsRelative[0].length();
	}
}
