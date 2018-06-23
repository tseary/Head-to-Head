package blasteroids;

import java.awt.Polygon;

import geometry.Vector2D;
import headtohead.Player;

public class Fragment extends RotatablePhysicsObject implements IOwnable, IPolygon {
	
	private Player owner;
	
	private Vector2D[] outlineRelativeVectors;
	
	public Fragment(Vector2D[] newOutline, Player owner) {
		// Make the position the average of the outline points
		for (int i = 0; i < newOutline.length; i++) {
			position.add(newOutline[i]);
		}
		position = position.scalarProduct(1d / newOutline.length);
		
		// Copy the outline vectors, relative to the position
		outlineRelativeVectors = new Vector2D[newOutline.length];
		for (int i = 0; i < outlineRelativeVectors.length; i++) {
			outlineRelativeVectors[i] = newOutline[i].difference(position);
		}
		
		// Set the owner
		this.owner = owner;
	}
	
	@Override
	public Vector2D[] getOutlineVectors(double extrapolateTime) {
		Vector2D[] outlineVectors = new Vector2D[outlineRelativeVectors.length];
		Vector2D outlinePosition = IPolygon.extrapolatePosition(this, extrapolateTime);
		for (int i = 0; i < outlineVectors.length; i++) {
			Vector2D vectorClone = outlineRelativeVectors[i].clone();
			vectorClone.rotate(angle);
			outlineVectors[i] = outlinePosition.sum(vectorClone);
		}
		return outlineVectors;
	}
	
	@Override
	public Polygon getOutline(double extrapolateTime) {
		return IPolygon.vectorsToPolygon(getOutlineVectors(extrapolateTime));
	}
	
	@Override
	public double getRadius() {
		return 0;
	}
	
	@Override
	public Player getOwner() {
		return owner;
	}
	
	@Override
	public String toString() {
		return "Fragment, velocity = " + velocity.length();
	}
}
