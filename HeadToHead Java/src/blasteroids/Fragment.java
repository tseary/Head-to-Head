package blasteroids;

import java.awt.Polygon;

import geometry.Vector2DLong;
import headtohead.IOwnable;
import headtohead.Player;
import physics.IPolygon;
import physics.RotatablePhysicsObject;

public class Fragment extends RotatablePhysicsObject implements IOwnable, IPolygon {
	
	private Player owner;
	
	private Vector2DLong[] outlineRelativeVectors;
	
	/**
	 * 
	 * @param newOutline The outline in physics units.
	 * @param owner
	 */
	public Fragment(Vector2DLong[] newOutline, Player owner) {
		// Make the position the average of the outline points
		for (int i = 0; i < newOutline.length; i++) {
			position.add(newOutline[i]);
		}
		position = position.scalarProduct(1d / newOutline.length);
		
		// Copy the outline vectors, relative to the position
		outlineRelativeVectors = new Vector2DLong[newOutline.length];
		for (int i = 0; i < outlineRelativeVectors.length; i++) {
			outlineRelativeVectors[i] = newOutline[i].difference(position);
		}
		
		// Set the owner
		this.owner = owner;
	}
	
	@Override
	public Vector2DLong[] getOutlineVectors(long extrapolateTime) {
		Vector2DLong[] outlineVectors = new Vector2DLong[outlineRelativeVectors.length];
		Vector2DLong outlinePosition = IPolygon.extrapolatePosition(this, extrapolateTime);
		for (int i = 0; i < outlineVectors.length; i++) {
			outlineVectors[i] = outlinePosition.sum(outlineRelativeVectors[i].getRotated(angle));
		}
		return outlineVectors;
	}
	
	@Override
	public Polygon getOutline(long extrapolateTime) {
		return IPolygon.vectorsToPolygon(getOutlineVectors(extrapolateTime));
	}
	
	@Override
	public long getRadius() {
		return 0;
	}
	
	@Override
	public double getMass() {
		return 0d;
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
