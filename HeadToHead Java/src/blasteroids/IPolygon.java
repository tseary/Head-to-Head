package blasteroids;

import java.awt.Polygon;

import geometry.Vector2D;

public interface IPolygon {
	public Vector2D[] getOutlineVectors();
	
	public Polygon getOutline();
	
	/**
	 * Converts an array of position vectors to a polygon.
	 * 
	 * @param outline
	 * @return
	 */
	static Polygon vectorsToPolygon(Vector2D[] outline) {
		int[] xPoints = new int[outline.length];
		int[] yPoints = new int[outline.length];
		for (int i = 0; i < outline.length; i++) {
			xPoints[i] = (int) outline[i].x;
			yPoints[i] = (int) outline[i].y;
		}
		return new Polygon(xPoints, yPoints, outline.length);
	}
}
