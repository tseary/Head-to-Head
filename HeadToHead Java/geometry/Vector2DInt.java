package geometry;

public class Vector2DInt {
	public int x, y;
	
	public Vector2DInt(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2DInt clone() {
		return new Vector2DInt(this.x, this.y);
	}
	
	public Vector2DInt difference(Vector2DInt v) {
		return new Vector2DInt(this.x - v.x, this.y - v.y);
	}
	
	public Vector2DInt scalarProduct(int s) {
		return new Vector2DInt(s * this.x, s * this.y);
	}
	
	public Vector2DInt sum(Vector2DInt v) {
		return new Vector2DInt(this.x + v.x, this.y + v.y);
	}
	
	@Override
	public String toString() {
		return "Vector2DInt[x=" + this.x + ",y=" + this.y + "]";
	}
}
