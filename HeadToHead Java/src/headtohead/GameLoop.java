package headtohead;

public class GameLoop implements Runnable {
	
	private HeadToHeadGameCanvas game;
	
	// DEBUG
	private double averageFrameMillis = 0d;
	private final double smoothing = 0.95d;
	
	public GameLoop(HeadToHeadGameCanvas game) {
		this.game = game;
	}
	
	// See http://gameprogrammingpatterns.com/game-loop.html
	@Override
	public void run() {
		final long PHYSICS_TICK_MILLIS = game.getPhysicsTickMillis();
		
		long previous = System.currentTimeMillis();
		long lagMillis = 0;
		
		// DEBUG
		int counter = 0;
		
		// Clamp the frame rate
		final long MIN_RENDER_MILLIS = 1000 / 60;
		
		while (!Thread.interrupted()) {
			long current = System.currentTimeMillis();
			long elapsed = current - previous;
			previous = current;
			lagMillis += elapsed;
			
			// game.processInput();
			
			// Do physics ticks until we're caught up
			while (lagMillis >= PHYSICS_TICK_MILLIS) {
				game.physicsTick();
				lagMillis -= PHYSICS_TICK_MILLIS;
			}
			
			game.render();
			
			// Calculate the average frame rate
			if (DebugMode.isEnabled()) {
				averageFrameMillis = smoothing * averageFrameMillis + (1d - smoothing) * elapsed;
				if (++counter >= 100) {
					System.out.println(averageFrameMillis);
					counter = 0;
				}
			}
			
			// Reduce the frame rate if necessary
			long sleepTime = MIN_RENDER_MILLIS - (System.currentTimeMillis() - current);
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}
}