package sound;

import java.net.URL;

import javax.sound.sampled.Clip;

public class SingleVoiceSet extends VoiceSet {
	
	private Clip voice;
	
	public SingleVoiceSet(URL url) {
		voice = loadClip(url);
	}
	
	@Override
	public void play() {
		voice.start();
	}
}
