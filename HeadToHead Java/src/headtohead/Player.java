package headtohead;

import java.awt.Color;

public class Player {
	
	private ArcadeButton[] buttons;
	
	private Color color;
	
	public int score = 0;
	
	public Player(ArcadeButton[] buttons, Color color) {
		setButtons(buttons);
		this.color = color;
	}
	
	/**
	 * Sets the buttons that this player can use.
	 * 
	 * @param buttons
	 */
	public void setButtons(ArcadeButton[] buttons) {
		this.buttons = new ArcadeButton[buttons.length];
		for (int i = 0; i < this.buttons.length; i++) {
			this.buttons[i] = buttons[i];
		}
	}
	
	public ArcadeButton getButton(int i) {
		return buttons[i];
	}
	
	public Color getColor() {
		return color;
	}
	
	@Override
	public String toString() {
		String string = color.toString() + "\t";
		for (ArcadeButton button : buttons) {
			string += (button.isPressed() ? button.getPressCounter() : 0) + " ";
		}
		return string;
	}
}
