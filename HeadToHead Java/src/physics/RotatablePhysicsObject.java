package physics;

public abstract class RotatablePhysicsObject extends PhysicsObject {
	
	public double angle;
	public double angularVelocity;
	public double angularAcceleration;
	
	public RotatablePhysicsObject() {
		super();
		angle = 0d;
		angularVelocity = 0d;
		angularAcceleration = 0d;
	}
	
	public RotatablePhysicsObject(PhysicsObject obj) {
		super(obj);
		angle = 0d;
		angularVelocity = 0d;
		angularAcceleration = 0d;
	}
	
	public RotatablePhysicsObject(RotatablePhysicsObject obj) {
		super(obj);
		this.angle = obj.angle;
		this.angularVelocity = obj.angularVelocity;
		this.angularAcceleration = obj.angularAcceleration;
	}
	
	@Override
	public void move(long deltaTime) {
		super.move(deltaTime);
		angle += (angularVelocity + angularAcceleration * deltaTime / 2d) * deltaTime;
		angularVelocity += angularAcceleration * deltaTime;
	}
}
