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
			// The object's position in this rectangle's coordinate space
			Vector2D circleCenter = obj.position.difference(position).getRotated(-angle);
			final double circleRadius = obj.getRadius();
			
			Vector2D wideSize = new Vector2D(size.x + circleRadius, size.y);
			if (Math.abs(circleCenter.x) <= wideSize.x && Math.abs(circleCenter.y) <= wideSize.y) {
				// Object is touching on the left or right ends of the rectangle, or in the middle
				return true;
			}
			
			Vector2D tallSize = new Vector2D(size.x, size.y + circleRadius);
			if (Math.abs(circleCenter.x) <= tallSize.x && Math.abs(circleCenter.y) <= tallSize.y) {
				// Object is touching on the top or bottom of the rectangle, or in the middle
				return true;
			}
			
			// True if any vertex is inside the object's radius
			Vector2D circleCenterAbs = new Vector2D(Math.abs(circleCenter.x), Math.abs(circleCenter.y));
			return circleCenterAbs.difference(size).lengthSquared() <= circleRadius * circleRadius;
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
