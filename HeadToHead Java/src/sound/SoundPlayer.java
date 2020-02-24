package sound;

import java.net.URL;

public class SoundPlayer {
	
	private boolean[] soundRequests;
	
	/**
	 * An array of audio clips.
	 * The members of the array may be null if the sound has not been loaded yet.
	 * Each VoiceSet contains one or more clips containing the same sound.
	 * The clips are rewound as they finish playing by the LineListener.
	 */
	private VoiceSet[] voiceSets;
	
	private boolean soundOn = true;
	
	public SoundPlayer() {
		int soundCount = SoundName.values().length;
		soundRequests = new boolean[soundCount];
		voiceSets = new VoiceSet[soundCount];
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
		for (SoundName soundName : SoundName.values()) {
			int i = soundName.ordinal();
			if (soundRequests[i]) {
				playSound(soundName);
				soundRequests[i] = false;
			} else if (soundName.getType() == SoundName.TYPE_LOOPING) {
				stopSound(soundName);
			}
		}
	}
	
	public void playSound(SoundName soundName) {
		int soundIndex = soundName.ordinal();
		
		// Lazily create lists
		// TODO Use the correct VoiceSet type
		if (voiceSets[soundIndex] == null) {
			VoiceSet voiceSet;
			URL url = getSoundURL(soundName);
			switch (soundName.getType()) {
				case SoundName.TYPE_SINGLE:
					voiceSet = new SingleVoiceSet(url);
					break;
				default:
				case SoundName.TYPE_MULTI:
					voiceSet = new MultiVoiceSet(url);
					break;
				case SoundName.TYPE_LOOPING:
					voiceSet = new LoopingVoiceSet(url);
					break;
			}
			voiceSets[soundIndex] = voiceSet;
		}
		
		// Play the sound
		voiceSets[soundIndex].play();
	}
	
	public void stopSound(SoundName soundName) {
		int soundIndex = soundName.ordinal();
		
		if (voiceSets[soundIndex] == null) return;
		
		// Stop the sound
		voiceSets[soundIndex].stop();
	}
	
	private static URL getSoundURL(SoundName soundName) {
		String path = "/soundfx/" + soundName.name().toLowerCase() + ".wav";
		return SoundPlayer.class.getResource(path);
	}
}
