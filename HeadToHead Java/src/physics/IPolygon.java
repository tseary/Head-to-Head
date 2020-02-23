package physics;

import java.awt.Polygon;

import geometry.Vector2DLong;

public interface IPolygon {
	
	/**
	 * Gets the physics outline of the object (units of PhysicsObject.DISTANCE_UNIT).
	 */
	public Vector2DLong[] getOutlineVectors(long extrapolateTime);
	
	/**
	 * Gets the aesthetic outline of the object (units of pixels).
	 */
	public Polygon getOutline(long extrapolateTime);
	
	/**
	 * Converts an array of position vectors to a java.awt.Polygon.
	 * 
	 * @param physOutline
	 * @return
	 */
	static Polygon vectorsToPolygon(Vector2DLong[] physOutline) {
		int[] xPoints = new int[physOutline.length];
		int[] yPoints = new int[physOutline.length];
		for (int i = 0; i < physOutline.length; i++) {
			xPoints[i] = PhysicsConstants.distanceToPixels(physOutline[i].x);
			yPoints[i] = PhysicsConstants.distanceToPixels(physOutline[i].y);
		}
		return new Polygon(xPoints, yPoints, physOutline.length);
	}
	
	public static Vector2DLong extrapolatePosition(PhysicsObject obj, long extrapolateTime) {
		return obj.position.sum(obj.velocity.scalarProduct(extrapolateTime));
	}
}
