package button;

public class VirtualButton implements IButton {
	
	public void press() {
		if (pressed) {
			return;
		}
		pressed = true;
		pressCounter++;
	}
	
	public void release() {
		pressed = false;
	}
	
	private boolean pressed = false;
	private int pressCounter = 0;
	
	@Override
	public boolean isPressed() {
		return pressed;
	}
	
	@Override
	public int getPressCounter() {
		return pressCounter;
	}
	
	@Override
	public void resetPressCounter() {
		pressCounter = 0;
	}
}
