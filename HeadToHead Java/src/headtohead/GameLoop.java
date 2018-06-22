package headtohead;

public class GameLoop implements Runnable {
	
	private HeadToHeadGamePanel game;
	
	public GameLoop(HeadToHeadGamePanel game) {
		this.game = game;
	}
	
	// See http://gameprogrammingpatterns.com/game-loop.html
	@Override
	public void run() {
		final long PHYSICS_TICK_MILLIS = game.getPhysicsTickMillis();
		
		long previous = System.currentTimeMillis();
		long lagMillis = 0;
		
		// Clamp the frame rate
		final long MIN_RENDER_MILLIS = 1000 / 30 + 1;
		
		while (!Thread.interrupted()) {
			long current = System.currentTimeMillis();
			long elapsed = current - previous;
			previous = current;
			lagMillis += elapsed;
			
			//game.processInput();
			
			// Do physics ticks until we're caught up
			while (lagMillis >= PHYSICS_TICK_MILLIS) {
				game.physicsTick();
				lagMillis -= PHYSICS_TICK_MILLIS;
			}
			
			game.render();
			
			// Reduce the frame rate if necessary
			long idleUntil = current + MIN_RENDER_MILLIS;
			while (System.currentTimeMillis() < idleUntil) {
				Thread.yield();
			}
		}
	}
}
