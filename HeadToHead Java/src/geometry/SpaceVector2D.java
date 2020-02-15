package geometry;

public class SpaceVector2D {
	public Vector2D position;
	public Vector2D vector;
	
	public SpaceVector2D() {}
	
	public SpaceVector2D(Vector2D position, Vector2D vector) {
		this.position = position.clone();
		this.vector = vector.clone();
	}
}
