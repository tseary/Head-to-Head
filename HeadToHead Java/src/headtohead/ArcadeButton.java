package headtohead;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * A physical button, bound to a keyboard key.
 * 
 * @author Thomas
 *
 */
public class ArcadeButton implements IButton, KeyListener {
	
	protected int keyCode;
	protected boolean pressed = false;
	protected int pressCounter = 0;
	
	public ArcadeButton(int keyCode) {
		this.keyCode = keyCode;
	}
	
	public boolean isPressed() {
		return pressed;
	}
	
	public int getPressCounter() {
		return pressCounter;
	}
	
	public void resetPressCounter() {
		pressCounter = 0;
	}
	
	// TODO What is this?
	/*public void keyPressed() {
		// Ignore repeated press events
		if (pressed) {
			return;
		}
		pressed = true;
		pressCounter++;
	}*/
	
	@Override
	public void keyTyped(KeyEvent e) {}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() != keyCode || pressed) {
			return;
		}
		pressed = true;
		pressCounter++;
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() != keyCode) {
			return;
		}
		pressed = false;
	}
}
