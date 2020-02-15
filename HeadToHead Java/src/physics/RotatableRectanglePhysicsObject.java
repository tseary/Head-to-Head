package physics;

import java.awt.Polygon;

import geometry.SpaceVector2D;
import geometry.Vector2D;
import headtohead.DebugMode;

public class RotatableRectanglePhysicsObject extends RotatablePhysicsObject implements IPolygon {
	
	/**
	 * The size vector points from the center of the rectangle to the vertex
	 * in the first quadrant, in this rectangle's coordinate space.
	 */
	protected Vector2D size;
	protected double boundingRadius;
	
	// The angle spanned by the top face of the rectangle.
	// The size vector rotated by this angle equals the size vector in quadrant 2.
	private double sizeQuad2Angle;
	
	public RotatableRectanglePhysicsObject(double width, double height) {
		super();
		size = new Vector2D(width / 2d, height / 2d);
		boundingRadius = size.length();
		sizeQuad2Angle = new Vector2D(-size.x, size.y).angle() - size.angle();
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
	
	/**
	 * Creates a unit vector pointing from the surface of this rectangle to the point,
	 * and a position vector indicating the nearest point on the surface to the given point.
	 * @param point
	 * @param outSurface A position vector indicating the closest point
	 * @param outNormal
	 */
	public SpaceVector2D getSurfaceNormal(Vector2D point) {
		// The object's position in this rectangle's coordinate space
		Vector2D pointRel = point.difference(position).getRotated(-angle);
		
		boolean rightOutside = pointRel.x > size.x,
				leftOutside = pointRel.x < -size.x;
		boolean aboveOutside = pointRel.y > size.y,
				belowOutside = pointRel.y < -size.y;
		
		SpaceVector2D surfaceNormal = new SpaceVector2D();
		
		if (rightOutside) {
			// Right
			if (aboveOutside) {
				// Above right (quad 1)
				surfaceNormal.position = position.sum(size.getRotated(angle));
				surfaceNormal.vector = point.difference(surfaceNormal.position).toUnit();
			} else if (belowOutside) {
				// Below right (quad 4)
				surfaceNormal.position = position.sum(size.getRotated(angle + Math.PI + sizeQuad2Angle));
				surfaceNormal.vector = point.difference(surfaceNormal.position).toUnit();
			} else {
				// Right side
				Vector2D surfacePosInRectCoords = new Vector2D(size.x, pointRel.y);
				surfaceNormal.position = position.sum(surfacePosInRectCoords.getRotated(angle));
				surfaceNormal.vector = new Vector2D(1d, angle, true);
			}
		} else if (leftOutside) {
			// Left
			if (aboveOutside) {
				// Above left (quad 2)
				surfaceNormal.position = position.sum(size.getRotated(angle + sizeQuad2Angle));
				surfaceNormal.vector = point.difference(surfaceNormal.position).toUnit();
			} else if (belowOutside) {
				// Below left (quad 3)
				surfaceNormal.position = position.sum(size.getRotated(angle + Math.PI));
				surfaceNormal.vector = point.difference(surfaceNormal.position).toUnit();
			} else {
				// Left side
				Vector2D surfacePosInRectCoords = new Vector2D(-size.x, pointRel.y);
				surfaceNormal.position = position.sum(surfacePosInRectCoords.getRotated(angle));
				surfaceNormal.vector = new Vector2D(-1d, angle, true);
			}
		} else {
			// Middle x
			if (aboveOutside) {
				// Above middle
				Vector2D surfacePosInRectCoords = new Vector2D(pointRel.x, size.y);
				surfaceNormal.position = position.sum(surfacePosInRectCoords.getRotated(angle));
				surfaceNormal.vector = new Vector2D(1d, angle + 0.5d * Math.PI, true);
			} else if (belowOutside) {
				// Below middle
				Vector2D surfacePosInRectCoords = new Vector2D(pointRel.x, -size.y);
				surfaceNormal.position = position.sum(surfacePosInRectCoords.getRotated(angle));
				surfaceNormal.vector = new Vector2D(1d, angle - 0.5d * Math.PI, true);
			} else {
				// Inside rectangle
				// TODO
				/*boolean rightCenter = pointRel.x >= 0d,
						aboveCenter = pointRel.y >= 0d;
				
				double distanceToXSide = size.x - Math.abs(pointRel.x),
						distanceToYSide = size.y - Math.abs(pointRel.y);
				
				boolean closeToXSide = distanceToXSide < distanceToYSide;
				if (closeToXSide) {
					if (rightCenter) {
						// TODO same as right side
					} else {
						// TODO same as left side
					}
				} else {
					if (aboveCenter) {
						// TODO same as top side
					} else {
						// TODO same as bottom side
					}
				}*/
				
				// PLACEHOLDER
				// surfaceNormal position equals point,
				// surfaceNormal vector points away from rectangle center
				surfaceNormal.position = point.clone();
				surfaceNormal.vector = point.difference(position).toUnit();
			}
		}
		
		return surfaceNormal;
		
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
