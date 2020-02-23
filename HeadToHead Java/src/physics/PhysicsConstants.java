package physics;

import geometry.Vector2DLong;

public final class PhysicsConstants {
	
	private static long DISTANCE_UNIT = 100,
			TIME_UNIT = 1000;
	
	/** The inverse distance unit relates pixels to physics units. */
	private static double DISTANCE_UNIT_INV = 1d / DISTANCE_UNIT;
	/** The inverse time unit relates velocity to distance, or acceleration to velocity. */
	private static double TIME_UNIT_INV = 1d / TIME_UNIT;
	
	private static double VELOCITY_UNIT = DISTANCE_UNIT * TIME_UNIT_INV,
			ACCELERATION_UNIT = VELOCITY_UNIT * TIME_UNIT_INV;
	private static double ANGULAR_VELOCITY_UNIT = TIME_UNIT_INV,
			ANGULAR_ACCELERATION_UNIT = ANGULAR_VELOCITY_UNIT * TIME_UNIT_INV;
	
	private PhysicsConstants() {}
	
	public static long distance(int pixels) {
		return pixels * DISTANCE_UNIT;
	}
	
	public static long distance(double meters) {
		return (long)(meters * DISTANCE_UNIT);
	}
	
	public static double velocity(double meterPerSecond) {
		return meterPerSecond * VELOCITY_UNIT;
	}
	
	public static double acceleration(double meterPerSecondSqr) {
		return meterPerSecondSqr * ACCELERATION_UNIT;
	}
	
	/**
	 * Converts between units that are related by integration,
	 * e.g. acceleration w.r.t. speed, or angular velocity w.r.t. angle.
	 * @param i
	 * @return
	 */
	public static double integral(double i) {
		return i * TIME_UNIT_INV;
	}
	
	public static double angularVelocity(double radPerSecond) {
		return radPerSecond * ANGULAR_VELOCITY_UNIT;
	}
	
	public static double angularAcceleration(double radPerSecondSqr) {
		return radPerSecondSqr * ANGULAR_ACCELERATION_UNIT;
	}
	
	public static long time(double seconds) {
		return (long)(seconds * TIME_UNIT);
	}
	
	public static Vector2DLong distanceToPixels(Vector2DLong v) {
		return new Vector2DLong(PhysicsConstants.DISTANCE_UNIT_INV * v.x, PhysicsConstants.DISTANCE_UNIT_INV * v.y);
	}
	
	public static int distanceToPixels(long d) {
		return (int)(PhysicsConstants.DISTANCE_UNIT_INV * d);
	}
}
