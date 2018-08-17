package headtohead;

public interface IButton {
	/**
	 * Returns true if the button is pressed right now.
	 * 
	 * @return
	 */
	public boolean isPressed();
	
	/**
	 * Gets the number of times that this button has been pressed since the last
	 * call to resetPressCounter(). Use this for button-mashing applications.
	 * 
	 * @return
	 */
	public int getPressCounter();
	
	/**
	 * Clears the of times that this button has been pressed.
	 */
	public void resetPressCounter();
}
