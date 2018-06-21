package geometry;

/**
 * A general-purpose 2D vector with common mathematical functions.
 * @author Thomas
 */
public class Vector2D {
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 140127L;
	
	public double x = 0.0, y = 0.0;
	
	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2D(double xr, double yt, boolean polar) {
		if (polar) {
			this.x = xr * Math.cos(yt);
			this.y = xr * Math.sin(yt);
		} else {
			this.x = xr;
			this.y = yt;
		}
	}
	
	/**
	 * Adds v to this vector.
	 * @param v Any vector.
	 * @see <code>public Vector2D sum(Vector2D v)</code>
	 */
	public void add(Vector2D v) {
		this.x += v.x;
		this.y += v.y;
	}
	
	public double angle() {
		return Math.atan2(y, x);
	}
	
	@Override
	public Vector2D clone() {
		return new Vector2D(this.x, this.y);
	}
	
	/**
	 * Calculates the determinant of the 2x2 matrix composed of the operands.
	 * @param v Any vector.
	 * @return <code>this.x * v.y - this.y * v.x</code>
	 */
	public double crossProduct(Vector2D v) {
		return this.x * v.y - this.y * v.x;
	}
	
	/**
	 * @param v Any vector.
	 * @return This vector minus v. Neither operand is modified.
	 * @see <code>public void subtract(Vector2D v)</code>
	 */
	public Vector2D difference(Vector2D v) {
		return new Vector2D(this.x - v.x, this.y - v.y);
	}
	
	/**
	 * Calculates the sum of the products of the operands' respective x and y components.
	 * @param v Any vector.
	 * @return <code>this.x*v.x + this.y*v.y</code>
	 */
	public double dotProduct(Vector2D v) {
		return this.x * v.x + this.y * v.y;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Vector2D) {
			Vector2D oVector = (Vector2D) o;
			return this.x == oVector.x && this.y == oVector.y;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		long xBits = Double.doubleToLongBits(this.x);
		long yBits = Double.doubleToLongBits(this.y);
		return (int) (xBits ^ (xBits >>> 32) ^ yBits ^ (yBits >>> 32));
	}
	
	/**
	 * @param v A vector to compare with this vector.
	 * @return True if neither operand is the zero vector and
	 *         the operands have the same slope (regardless of direction).
	 */
	public boolean isParallel(Vector2D v) {
		if (this.x == 0d && this.y == 0d || v.x == 0d && v.y == 0d) {	// If either operand is [0,0]
			return false;
		}
		if (this.x != 0d && v.x != 0d) {	// Prevent division by zero
			return this.y / this.x == v.y / v.x;
		} else {
			return this.x / this.y == v.x / v.y;
		}
	}
	
	/**
	 * @return The length of this vector squared. (Use this to save square roots).
	 */
	public double lengthSquared() {
		return this.x * this.x + this.y * this.y;
	}
	
	/**
	 * @return The length of this vector.
	 */
	public double length() {
		return Math.sqrt(this.x * this.x + this.y * this.y);
	}
	
	/**
	 * Rotates this vector CCW by a.
	 * @param angle The angle in radians.
	 */
	public void rotate(double angle) {
		this.setAngle(this.angle() + angle);
	}
	
	/**
	 * @param s The scale factor.
	 * @return This vector scaled by s. This vector is not modified.
	 */
	public Vector2D scalarProduct(double s) {
		return new Vector2D(s * this.x, s * this.y);
	}
	
	/**
	 * Rotates the vector while preserving its length.
	 * @param a The new angle in radians.
	 */
	public void setAngle(double angle) {
		double length = this.length();
		this.x = length * Math.cos(angle);
		this.y = length * Math.sin(angle);
	}
	
	/**
	 * Scales the vector while preserving its angle.
	 * @param length The new length.
	 */
	public void setLength(double length) {
		double oldLength = this.length();
		if (oldLength == 0.0)
			return;
		length /= oldLength;
		this.x *= length;
		this.y *= length;
	}
	
	/**
	 * Subtracts v from this vector.
	 * @param v Any vector.
	 * @see <code>public Vector2D difference(Vector2D v)</code>
	 */
	public void subtract(Vector2D v) {
		this.x -= v.x;
		this.y -= v.y;
	}
	
	/**
	 * @param v Any vector.
	 * @return This vector plus v. Neither operand is modified.
	 * @see <code>public Vector2D add(Vector2D v)</code>
	 */
	public Vector2D sum(Vector2D v) {
		return new Vector2D(this.x + v.x, this.y + v.y);
	}
	
	@Override
	public String toString() {
		return "[" + this.x + "," + this.y + "]";
	}
	
	/**
	 * Creates a unit vector.
	 * @return A unit vector parallel to this vector.
	 */
	public Vector2D toUnit() {
		Vector2D unit = this.clone();
		unit.setLength(1d);
		return unit;
	}
}
