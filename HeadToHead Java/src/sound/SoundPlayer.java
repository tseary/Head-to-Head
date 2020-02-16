package sound;

import java.net.URL;

public class SoundPlayer {
	
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
	private VoiceSet[] voiceSets;
	
	private boolean soundOn = true;
	
	public SoundPlayer() {
		SoundName[] values = SoundName.values();
		
		soundRequests = new boolean[values.length];
		soundURLs = new URL[values.length];
		
		voiceSets = new VoiceSet[values.length];
		
		for (SoundName value : values) {
			// Note: URL path is case-sensitive in exported jar
			String path = "/soundfx/" + value.name().toLowerCase() + ".wav";
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
		// TODO Use the correct VoiceSet type
		if (voiceSets[soundIndex] == null) {
			voiceSets[soundIndex] = new MultiVoiceSet(soundURLs[soundIndex]);
		}
		
		// Play the sound
		voiceSets[soundIndex].play();
	}
}
