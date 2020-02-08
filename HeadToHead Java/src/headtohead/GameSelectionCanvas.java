package headtohead;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

public class GameSelectionCanvas extends HeadToHeadGameCanvas {
	private static final long serialVersionUID = 1L;
	
	private List<Image> gameThumbs;
	private List<Color> borderColors;
	
	public GameSelectionCanvas() {
		super(400);
		
		gameThumbs = new ArrayList<Image>();
		borderColors = new ArrayList<Color>();
	}
	
	public void addThumbnail(Image thumbnail, Color borderColor) {
		System.out.println("GameSelectionCanvas.addThumbnail()");
		
		gameThumbs.add(thumbnail);
		borderColors.add(borderColor);
	}
	
	@Override
	public void newGame() {
		System.out.println("GameSelectionCanvas.newGame()");
		
		startGameLoop();
	}
	
	@Override
	public void newRound() {
		System.out.println("GameSelectionCanvas.newRound()");
		
	}
	
	@Override
	public long getPhysicsTickMillis() {
		return 1000;
	}
	
	@Override
	protected void physicsTick() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void drawVideoFrame(Graphics g, double extrapolate) {
		System.out.println("GameSelectionCanvas.drawVideoFrame()");
		System.out.println("gameThumbs.size() = " + gameThumbs.size());
		
		// Clear background
		g.setColor(new Color(0x008080));
		g.fillRect(0, 0, getGameWidth(), getGameHeight());
		
		// Choose the size of the thumbnail grid
		final int thumbnailSize = 100;
		final int rows = 2, cols = 1;
		final int xPitch = 200, yPitch = 200;
		final int xOffset = (getGameWidth() - (cols - 1) * xPitch - thumbnailSize) / 2;
		final int yOffset = (getGameHeight() - (rows - 1) * yPitch - thumbnailSize) / 2;
		
		// Draw game thumbnails
		for (int i = 0; i < gameThumbs.size(); i++) {
			int x = xOffset + (i % cols) * xPitch;
			int y = yOffset + (i / cols) * yPitch;
			
			// Draw border
			final int borderSize = 6;
			g.setColor(borderColors.get(i));
			g.fill3DRect(x - borderSize, y - borderSize,
					thumbnailSize + 2 * borderSize, thumbnailSize + 2 * borderSize, true);
			
			// Draw image
			Image resizedThumbnail = gameThumbs.get(i).getScaledInstance(
					thumbnailSize, thumbnailSize, Image.SCALE_FAST);
			g.drawImage(resizedThumbnail, x, y, null);
		}
	}
	
}
