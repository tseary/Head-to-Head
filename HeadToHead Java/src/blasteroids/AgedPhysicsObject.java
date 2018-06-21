package blasteroids;

public abstract class AgedPhysicsObject extends PhysicsObject {
	/** The object's age in physics seconds. */
	protected double age = 0d;
	
	@Override
	public void move(double deltaTime) {
		super.move(deltaTime);
		age += deltaTime;
	}
	
	public double getAge() {
		return age;
	}
}
