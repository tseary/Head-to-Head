package headtohead;

// For a human player, a collection of arcade buttons
// For a computer player, an collection of emulated buttons

public class InputSource {
	public InputSource(IButton[] newButtons) {
		this.buttons = newButtons != null ? newButtons.clone() : new IButton[0];
		
		buttonRemap = new int[buttons.length];
		clearButtonRemap();
	}
	
	protected IButton[] buttons;
	protected int[] buttonRemap;
	
	public IButton getButton(int index) {
		return buttons[buttonRemap[index]];
	}
	
	public void setButtonRemap(int[] newButtonRemap) {
		for (int i = 0; i < buttonRemap.length; i++) {
			if (i < newButtonRemap.length && newButtonRemap[i] < buttonRemap.length) {
				buttonRemap[i] = newButtonRemap[i];
			} else {
				buttonRemap[i] = i;
			}
		}
	}
	
	public void clearButtonRemap() {
		for (int i = 0; i < buttonRemap.length; i++) {
			buttonRemap[i] = i;
		}
	}
	
	@Override
	public String toString() {
		String string = "";
		for (IButton button : buttons) {
			string += (button.isPressed() ? button.getPressCounter() : 0) + " ";
		}
		return string;
	}
}
