package geometry;

public class SpaceVector2DLong {
	public Vector2DLong position;
	public Vector2D vector;
	
	public SpaceVector2DLong() {}
	
	public SpaceVector2DLong(Vector2DLong position, Vector2D vector) {
		this.position = position.clone();
		this.vector = vector.clone();
	}
}
