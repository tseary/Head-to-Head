package sound;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;

public class SoundPlayer implements LineListener {
	
	private URL[] soundURLs;
	private boolean[] soundRequests;
	
	/**
	 * An array of lists of audio clips.
	 * The members of the array may be null if the sound has not been loaded yet.
	 * Each list contains one or more clips containing the same sound.
	 * The clips are rewound as they finish playing by the LineListener.
	 * If a sound is already playing and needs to be played again simultaneously,
	 * a new instance of the clip is added to the corresponding list.
	 */
	private List<Clip>[] clips;
	
	private boolean soundOn = true;
	
	@SuppressWarnings("unchecked")
	public SoundPlayer() {
		SoundName[] values = SoundName.values();
		
		soundRequests = new boolean[values.length];
		soundURLs = new URL[values.length];
		
		clips = new ArrayList[values.length];
		
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
		
		// Lazily create lists
		if (clips[soundIndex] == null) {
			clips[soundIndex] = new ArrayList<Clip>();
		}
		
		// Get the list of already-loaded clips
		List<Clip> voices = clips[soundIndex];
		
		// Find a voice that isn't in use and start it
		for (Clip voice : voices) {
			if (!voice.isRunning()) {
				voice.start();
				return;
			}
		}
		
		// No voices were available, so make another one and start it
		addVoiceAndStart(soundIndex);
	}
	
	private void addVoiceAndStart(int soundIndex) {
		try {
			// Load the URL
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURLs[soundIndex]);
			
			// Get a sound clip resource
			Clip voice = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream
			voice.open(audioIn);
			voice.addLineListener(this);
			
			// Put the clip in the list
			clips[soundIndex].add(voice);
			
			// Start playback
			voice.start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
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
	
	// DEBUG
	public void printVoiceStatistics() {
		SoundName[] values = SoundName.values();
		
		for (SoundName value : values) {
			System.out.print(value.name() + "\t");
			List<Clip> voices = clips[value.ordinal()];
			System.out.println(voices != null ? voices.size() : "null");
		}
	}
}
