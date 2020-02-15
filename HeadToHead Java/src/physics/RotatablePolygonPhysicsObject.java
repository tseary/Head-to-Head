package physics;

import java.awt.Polygon;

import geometry.Vector2D;
import headtohead.DebugMode;

@Deprecated
public abstract class RotatablePolygonPhysicsObject extends RotatablePhysicsObject implements IPolygon {
	@Override
	public boolean isTouching(PhysicsObject obj) {
		if (!(obj instanceof RotatablePolygonPhysicsObject)) {
			
			// Check if any point on the outline is inside the obj
			Vector2D[] outline = getOutlineVectors(0d);
			double objRadiusSqr = Math.pow(obj.getRadius(), 2d);
			for (Vector2D point : outline) {
				if (point.difference(obj.position).lengthSquared() <= objRadiusSqr) {
					return true;
				}
			}
			
			// If the outline isn't touching, do the base collision detection (cirle-circle)
			return super.isTouching(obj);
		}
		
		// Do polygon-polygon collision detection
		RotatablePolygonPhysicsObject obj1 = this;
		RotatablePolygonPhysicsObject obj2 = (RotatablePolygonPhysicsObject)obj;
		
		Vector2D axis = obj2.position.difference(obj1.position).toUnit();
		
		Vector2D[] obj1OutlineVectors = obj1.getOutlineVectors(0d);
		Vector2D[] obj2OutlineVectors = obj2.getOutlineVectors(0d);
		
		double obj1Max = obj1.position.dotProduct(axis),
				obj1Min = obj1Max;
		double obj2Max = obj2.position.dotProduct(axis),
				obj2Min = obj2Max;
		
		for (Vector2D vertex1 : obj1OutlineVectors) {
			double obj1ProjectedVertex = vertex1.dotProduct(axis);
			
			if (obj1ProjectedVertex > obj1Max) {
				obj1Max = obj1ProjectedVertex;
			} else if (obj1ProjectedVertex < obj1Min) {
				obj1Min = obj1ProjectedVertex;
			}
		}
		
		for (Vector2D vertex2 : obj2OutlineVectors) {
			double obj2ProjectedVertex = vertex2.dotProduct(axis);
			
			if (obj2ProjectedVertex > obj2Max) {
				obj2Max = obj2ProjectedVertex;
			} else if (obj2ProjectedVertex < obj2Min) {
				obj2Min = obj2ProjectedVertex;
			}
		}
		
		// boolean touching = obj1Max >= obj2Min;
		final double OVERLAP = (obj1.getRadius() + obj2.getRadius()) * 0.1d;
		boolean touching = obj1Max - obj2Min > OVERLAP;
		if (DebugMode.isEnabled() && touching) {
			System.out.println("Polygon-polygon collision");
		}
		return touching;
	}
	
	@Override
	public Polygon getOutline(double extrapolateTime) {
		return IPolygon.vectorsToPolygon(getOutlineVectors(extrapolateTime));
	}
}
