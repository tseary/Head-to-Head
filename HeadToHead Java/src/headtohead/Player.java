package headtohead;

import java.awt.Color;

import button.IButton;
import button.InputSource;

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
	
	public void setColor(Color newColor) {
		color = newColor;
	}
	
	public Color getColor() {
		return color;
	}
	
	@Override
	public String toString() {
		return color.toString() + "\t" + inputSource.toString();
	}
}
