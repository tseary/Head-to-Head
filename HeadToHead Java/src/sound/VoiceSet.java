package sound;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;

public abstract class VoiceSet implements LineListener {
	/**
	 * Start playing the sound on one voice.
	 */
	public abstract void play();
	
	@Override
	public void update(LineEvent event) {
		// Ignore everything except stop events
		if (event.getType() != Type.STOP) return;
		
		// Rewind the clip
		Line line = event.getLine();
		if (line instanceof Clip) {
			Clip clip = (Clip)line;
			clip.setFramePosition(0);
		}
	}
	
	protected Clip loadClip(URL url) {
		try {
			// Load the URL
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
			
			// Get a sound clip resource
			Clip clip = AudioSystem.getClip();
			
			// Open audio clip and load samples from the audio input stream
			clip.open(audioIn);
			clip.addLineListener(this);
			
			return clip;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
