package physics;

import java.awt.Polygon;

import geometry.Vector2D;
import headtohead.DebugMode;

public class RotatableRectanglePhysicsObject extends RotatablePhysicsObject implements IPolygon {
	
	protected Vector2D size;
	protected double boundingRadius;
	
	public RotatableRectanglePhysicsObject(double width, double height) {
		super();
		size = new Vector2D(width / 2d, height / 2d);
		boundingRadius = size.length();
	}
	
	@Override
	public boolean isTouching(PhysicsObject obj) {
		// Do AABB collision first
		if (!isTouchingAABB(obj)) return false;
		
		if (!(obj instanceof RotatableRectanglePhysicsObject)) {
			// Check if any point on the outline is inside the obj
			Vector2D[] outline = getOutlineVectors(0d);
			double objRadiusSqr = obj.getRadius();
			objRadiusSqr *= objRadiusSqr;
			for (Vector2D point : outline) {
				if (point.difference(obj.position).lengthSquared() <= objRadiusSqr) {
					return true;
				}
			}
			
			// If the outline isn't touching, do cirle-circle collision detection
			return isTouchingCircular(obj);
		}
		
		// Do rotated rectangle-rectangle collision detection
		// WARNING: This implementation only detects when a vertex from one
		// rectangle is inside the other. It cannot detect crossed rectangles.
		RotatableRectanglePhysicsObject obj1 = this;
		RotatableRectanglePhysicsObject obj2 = (RotatableRectanglePhysicsObject)obj;
		
		Vector2D[] obj1OutlineVectors = obj1.getOutlineVectors(0d);
		for (Vector2D vertex1 : obj1OutlineVectors) {
			if (vertex1 == null) {
				if (DebugMode.isEnabled()) {
					System.out.println("null vertex1");
				}
				continue;
			}
			if (obj2.isPointInside(vertex1)) return true;
		}
		
		Vector2D[] obj2OutlineVectors = obj2.getOutlineVectors(0d);
		for (Vector2D vertex2 : obj2OutlineVectors) {
			if (vertex2 == null) {
				if (DebugMode.isEnabled()) {
					System.out.println("null vertex2");
				}
				continue;
			}
			if (obj1.isPointInside(vertex2)) return true;
		}
		
		return false;
	}
	
	/**
	 * Determines if a point is inside this rectangle.
	 * @param point
	 * @return
	 */
	protected boolean isPointInside(Vector2D point) {
		// Relative point implementation:
		Vector2D pointRel = point.difference(position);
		pointRel.rotate(-angle);
		return Math.abs(pointRel.x) <= size.x && Math.abs(pointRel.y) <= size.y;
	}
	
	@Override
	public double getRadius() {
		return boundingRadius;
	}
	
	@Override
	public double getMass() {
		return size.x * size.y * 4d;
	}
	
	@Override
	public Vector2D[] getOutlineVectors(double extrapolateTime) {
		Vector2D sizeQuad13 = size.getRotated(angle),
				sizeQuad24 = new Vector2D(-size.x, size.y);
		sizeQuad24.rotate(angle);
		return new Vector2D[] {
				position.sum(sizeQuad13),
				position.sum(sizeQuad24),
				position.difference(sizeQuad13),
				position.difference(sizeQuad24) };
	}
	
	@Override
	public Polygon getOutline(double extrapolateTime) {
		return IPolygon.vectorsToPolygon(getOutlineVectors(extrapolateTime));
	}
}
