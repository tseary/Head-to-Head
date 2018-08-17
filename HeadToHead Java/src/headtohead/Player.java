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
	
	public IButton getButton(int i) {
		return inputSource.buttons[i];
	}
	
	public Color getColor() {
		return color;
	}
	
	@Override
	public String toString() {
		return color.toString() + "\t" + inputSource.toString();
	}
}
