package sound;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundPlayer {
	
	private Map<String, URL> soundURLs;
	
	public SoundPlayer() {
		soundURLs = new HashMap<String, URL>();
		
		// TODO Make sound keys game-specific
		soundURLs.put("Pew", SoundPlayer.class.getResource("/soundfx/pew150ms.wav"));
		soundURLs.put("Bump", SoundPlayer.class.getResource("/soundfx/bump.wav"));
		soundURLs.put("Crack", SoundPlayer.class.getResource("/soundfx/crack.wav"));
		soundURLs.put("Crash", SoundPlayer.class.getResource("/soundfx/crash.wav"));
		soundURLs.put("Hit", SoundPlayer.class.getResource("/soundfx/hit2.wav"));
		soundURLs.put("GameOver", SoundPlayer.class.getResource("/soundfx/gameover3.wav"));
		soundURLs.put("GameOverP0", SoundPlayer.class.getResource("/soundfx/rightwins.wav"));
		soundURLs.put("GameOverP1", SoundPlayer.class.getResource("/soundfx/leftwins.wav"));
	}
	
	public void playSound(String soundName) {
		// Fail if the sound doesn't exist
		if (!soundURLs.containsKey(soundName)) {
			System.err.println("Sound doesn't exist: \"" + soundName + "\"");
			return;
		}
		
		try {
			// Load the URL
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURLs.get(soundName));
			
			// Get a sound clip resource.
			Clip clip = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream.
			clip.open(audioIn);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
