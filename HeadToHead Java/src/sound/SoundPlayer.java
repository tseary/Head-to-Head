package sound;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundPlayer {
	
	private URL[] soundURLs;
	private boolean[] soundRequests;
	
	private boolean soundOn = true;
	
	public SoundPlayer() {
		SoundName[] values = SoundName.values();
		
		soundRequests = new boolean[values.length];
		soundURLs = new URL[values.length];
		
		for (SoundName value : values) {
			String path = "/soundfx/" + value.name() + ".wav";
			soundURLs[value.ordinal()] = SoundPlayer.class.getResource(path);
			if (soundURLs[value.ordinal()] == null) {
				System.out.println("Failed to load sound URL \"" + path + "\"");
			}
		}
	}
	
	public void setSoundOn(boolean soundOn) {
		this.soundOn = soundOn;
	}
	
	public void clearRequests() {
		for (int i = 0; i < soundRequests.length; i++) {
			soundRequests[i] = false;
		}
	}
	
	public void request(SoundName soundName) {
		if (soundOn) soundRequests[soundName.ordinal()] = true;
	}
	
	public void playRequestedSounds() {
		if (!soundOn) {
			clearRequests();
			return;
		}
		for (int i = 0; i < soundRequests.length; i++) {
			if (!soundRequests[i]) continue;
			playSound(i);
			soundRequests[i] = false;
		}
	}
	
	public void playSound(SoundName soundName) {
		playSound(soundName.ordinal());
	}
	
	public void playSound(int soundIndex) {
		// Fail if the sound doesn't exist
		if (soundIndex < 0 || soundIndex > soundURLs.length) {
			System.err.println("Sound doesn't exist: " + soundIndex);
			return;
		}
		
		try {
			// Load the URL
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURLs[soundIndex]);
			
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
