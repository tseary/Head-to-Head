package physics;

public abstract class AgedPhysicsObject extends PhysicsObject {
	/** The object's age in physics time units. */
	protected long age = 0;
	
	@Override
	public void move(long deltaTime) {
		super.move(deltaTime);
		age += deltaTime;
	}
	
	public long getAge() {
		return age;
	}
	
	public void addAge(long deltaTime) {
		age += deltaTime;
	}
}
