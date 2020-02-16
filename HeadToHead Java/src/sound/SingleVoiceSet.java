package sound;

import java.net.URL;

import javax.sound.sampled.Clip;

public class SingleVoiceSet extends VoiceSet {
	
	protected Clip voice;
	
	public SingleVoiceSet(URL url) {
		voice = loadClip(url);
	}
	
	@Override
	public void play() {
		if (voice == null) return;
		voice.start();
	}
	
	@Override
	public void stop() {
		if (voice == null) return;
		voice.stop();
	}
}
