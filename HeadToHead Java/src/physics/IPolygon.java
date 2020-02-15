package physics;

import java.awt.Polygon;

import geometry.Vector2D;

public interface IPolygon {
	
	/**
	 * Gets the physics outline of the object.
	 */
	public Vector2D[] getOutlineVectors(double extrapolateTime);
	
	/**
	 * Gets the aesthetic outline of the object.
	 */
	public Polygon getOutline(double extrapolateTime);
	
	/**
	 * Converts an array of position vectors to a java.awt.Polygon.
	 * 
	 * @param outline
	 * @return
	 */
	static Polygon vectorsToPolygon(Vector2D[] outline) {
		int[] xPoints = new int[outline.length];
		int[] yPoints = new int[outline.length];
		for (int i = 0; i < outline.length; i++) {
			xPoints[i] = (int)outline[i].x;
			yPoints[i] = (int)outline[i].y;
		}
		return new Polygon(xPoints, yPoints, outline.length);
	}
	
	public static Vector2D extrapolatePosition(PhysicsObject obj, double extrapolateTime) {
		return obj.position.sum(obj.velocity.scalarProduct(extrapolateTime));
	}
}
