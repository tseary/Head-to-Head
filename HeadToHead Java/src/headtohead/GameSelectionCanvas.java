package headtohead;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

public class GameSelectionCanvas extends HeadToHeadGameCanvas {
	private static final long serialVersionUID = 1L;
	
	// Thumbnails
	private List<Image> gameThumbs;
	private List<Color> borderColors;
	
	// The chosen game
	private int selectedGameIndex = -1;
	
	public GameSelectionCanvas() {
		super(400);
		
		gameThumbs = new ArrayList<Image>();
		borderColors = new ArrayList<Color>();
	}
	
	/**
	 * Adds a game thumbnail to the list of available games.
	 * @param thumbnail
	 * @param borderColor
	 * @return The index assigned to the game. This matches the value
	 *         returned by getSelectedIndex() if this game is chosen.
	 */
	public int addThumbnail(Image thumbnail, Color borderColor) {
		gameThumbs.add(thumbnail);
		borderColors.add(borderColor);
		return gameThumbs.size() - 1;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getSelectedIndex() {
		return selectedGameIndex;
	}
	
	@Override
	public void newGame() {}
	
	@Override
	public void newRound() {}
	
	@Override
	public long getPhysicsTickMillis() {
		return 1000 / 60;
	}
	
	@Override
	protected void physicsTick() {
		// Read buttons, select game
		// TODO Change to left/right enter to support > 3 games.
		// TODO Require player consensus
		for (int g = 0; g < gameThumbs.size() && selectedGameIndex < 0; g++) {
			for (Player player : players) {
				if (player.getButton(g).isPressed()) {
					selectedGameIndex = g;
					break;
				}
			}
		}
		
		if (selectedGameIndex >= 0) {
			try {
				stopGameLoop();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void drawVideoFrame(Graphics g, double extrapolate) {
		// Clear background
		g.setColor(new Color(0x008080));
		g.fillRect(0, 0, getGameWidthPixels(), getGameHeightPixels());
		
		// Choose the size of the thumbnail grid
		final int thumbnailSize = 100;
		final int rows = 2, cols = 2;
		final int xPitch = 200, yPitch = 200;
		final int xOffset = (getGameWidthPixels() - (cols - 1) * xPitch - thumbnailSize) / 2;
		final int yOffset = (getGameHeightPixels() - (rows - 1) * yPitch - thumbnailSize) / 2;
		
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
