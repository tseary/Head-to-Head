package headtohead;

// For a human player, a collection of arcade buttons
// For a computer player, an collection of emulated buttons

// TODO Maybe InputSource should direct KeyEvents to ArcadeButtons

public class InputSource {
	public InputSource(IButton[] newButtons) {
		this.buttons = newButtons != null ? newButtons.clone() : new IButton[0];
	}
	
	public IButton[] buttons;
	
	@Override
	public String toString() {
		String string = "";
		for (IButton button : buttons) {
			string += (button.isPressed() ? button.getPressCounter() : 0) + " ";
		}
		return string;
	}
}
