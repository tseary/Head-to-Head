package geometry;

public class Vector2DLong {
	
	public long x, y;
	
	public Vector2DLong() {
		this(0, 0);
	}
	
	public Vector2DLong(long x, long y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2DLong(double x, double y) {
		this.x = (long)x;
		this.y = (long)y;
	}
	
	public Vector2DLong(Vector2D v) {
		this.x = (long)v.x;
		this.y = (long)v.y;
	}
	
	public Vector2DLong(double xr, double yt, boolean polar) {
		if (polar) {
			this.x = (long)(xr * Math.cos(yt));
			this.y = (long)(xr * Math.sin(yt));
		} else {
			this.x = (long)xr;
			this.y = (long)yt;
		}
	}
	
	public void add(Vector2DLong v) {
		this.x += v.x;
		this.y += v.y;
	}
	
	public void add(Vector2D v) {
		this.x += (long)v.x;
		this.y += (long)v.y;
	}
	
	public double angle() {
		return Math.atan2(y, x);
	}
	
	public Vector2DLong clone() {
		return new Vector2DLong(this.x, this.y);
	}
	
	public Vector2DLong difference(Vector2DLong v) {
		return new Vector2DLong(this.x - v.x, this.y - v.y);
	}
	
	public long dotProduct(Vector2DLong v) {
		return this.x * v.x + this.y * v.y;
	}
	
	public double dotProduct(Vector2D v) {
		return this.x * v.x + this.y * v.y;
	}
	
	public Vector2DLong scalarProduct(long s) {
		return new Vector2DLong(s * this.x, s * this.y);
	}
	
	public Vector2DLong scalarProduct(double s) {
		return new Vector2DLong((long)(s * this.x), (long)(s * this.y));
	}
	
	public Vector2DLong sum(Vector2DLong v) {
		return new Vector2DLong(this.x + v.x, this.y + v.y);
	}
	
	public Vector2DLong sum(Vector2D v) {
		return new Vector2DLong(this.x + (long)v.x, this.y + (long)v.y);
	}
	
	@Override
	public String toString() {
		return "Vector2DInt[x=" + this.x + ",y=" + this.y + "]";
	}
	
	public double length() {
		return Math.sqrt(x * x + y * y);
	}
	
	public long lengthSquared() {
		return x * x + y * y;
	}
	
	public Vector2D toUnit() {
		double scale = 1d / this.length();
		return new Vector2D(scale * x, scale * y);
	}
	
	public void rotate(double angle) {
		this.setAngle(this.angle() + angle);
	}
	
	public Vector2DLong getRotated(double angle) {
		double length = this.length();
		double newAngle = this.angle() + angle;
		return new Vector2DLong((long)(length * Math.cos(newAngle)), (long)(length * Math.sin(newAngle)));
	}
	
	public void setAngle(double angle) {
		double length = this.length();
		this.x = (long)(length * Math.cos(angle));
		this.y = (long)(length * Math.sin(angle));
	}
	
	public void setLength(double length) {
		double scale = length / this.length();
		this.x = (long)(scale * x);
		this.y = (long)(scale * y);
	}
	
	public void subtract(Vector2DLong v) {
		this.x -= v.x;
		this.y -= v.y;
	}
	
}
