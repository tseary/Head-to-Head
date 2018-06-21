package blasteroids;

public abstract class RotatablePhysicsObject extends PhysicsObject {
	
	public double angle;
	public double angularVelocity;
	
	public RotatablePhysicsObject() {
		super();
		angle = 0d;
		angularVelocity = 0d;
	}
	
	public RotatablePhysicsObject(PhysicsObject obj) {
		super(obj);
		angle = 0d;
		angularVelocity = 0d;
	}
	
	public RotatablePhysicsObject(RotatablePhysicsObject obj) {
		super(obj);
		this.angle = obj.angle;
		this.angularVelocity = obj.angularVelocity;
	}
	
	@Override
	public void move(double deltaTime) {
		super.move(deltaTime);
		angle += deltaTime * angularVelocity;
	}
}
