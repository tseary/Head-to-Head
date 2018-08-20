package headtohead;

import java.awt.Color;

public class Player {
	/**
	 * The buttons that control the player's actions.
	 */
	private InputSource inputSource;
	
	private Color color;
	
	public int score = 0;
	
	public Player(InputSource inputSource, Color color) {
		setInputSource(inputSource);
		this.color = color;
	}
	
	/**
	 * Sets the buttons that this player can use.
	 * 
	 * @param buttons
	 */
	public void setInputSource(InputSource inputSource) {
		this.inputSource = inputSource;
	}
	
	public InputSource getInputSource() {
		return inputSource;
	}
	
	public IButton getButton(int index) {
		return inputSource.getButton(index);
	}
	
	public Color getColor() {
		return color;
	}
	
	@Override
	public String toString() {
		return color.toString() + "\t" + inputSource.toString();
	}
}
