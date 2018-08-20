package headtohead;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.Timer;

public class DemoInputSource extends InputSource implements ActionListener {
	
	private static Timer timer;
	private Random random;
	
	/**
	 * -1 = left 0 = stop +1 = right
	 */
	private int rotation = 0;
	
	public DemoInputSource(IButton[] buttons) {
		super(buttons);
		
		random = new Random();
		
		if (timer == null) {
			timer = new Timer(166, this);
			timer.start();
		} else {
			timer.addActionListener(this);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// Set the rotation
		rotation += random.nextInt(3) - 1;
		rotation = Math.min(Math.max(-2, rotation), 2);
		
		if (rotation == -1) {
			((VirtualButton) buttons[0]).press();
		} else if (rotation == 1) {
			((VirtualButton) buttons[1]).press();
		} else {
			((VirtualButton) buttons[0]).release();
			((VirtualButton) buttons[1]).release();
		}
		
		// Press the shoot button randomly
		if (random.nextBoolean()) {
			((VirtualButton) buttons[2]).press();
		} else {
			((VirtualButton) buttons[2]).release();
		}
	}
}
