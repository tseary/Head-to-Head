package sound;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.Clip;

public class MultiVoiceSet extends VoiceSet {
	
	private URL url;
	private List<Clip> voices;	// Members are guaranteed non-null
	
	public MultiVoiceSet(URL url) {
		this.url = url;
		voices = new ArrayList<Clip>();
	}
	
	/**
	 * If a sound is already playing and needs to be played again simultaneously,
	 * a new instance of the clip is loaded and played.
	 */
	public void play() {
		// Find a voice that isn't in use and start it
		for (Clip voice : voices) {
			if (!voice.isRunning()) {
				voice.start();
				return;
			}
		}
		
		// No voices were available, so make another one and start it
		addVoiceAndStart();
	}
	
	@Override
	public void stop() {
		for (Clip voice : voices) {
			voice.stop();
		}
	}
	
	private void addVoiceAndStart() {
		// Load a new clip
		Clip voice = loadClip(url);
		if (voice == null) return;
		
		// Put the clip in the list
		voices.add(voice);
		
		// Start playback
		voice.start();
	}
}
