package sound;

import java.net.URL;

import javax.sound.sampled.Clip;

public class LoopingVoiceSet extends SingleVoiceSet {
	
	public LoopingVoiceSet(URL url) {
		super(url);
	}
	
	@Override
	public void play() {
		if (voice == null) return;
		voice.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	public void stop() {
		if (voice == null) return;
		
		// Finish this loop and stop
		// if (voice.isRunning()) voice.loop(0);
		
		// Stop immediately
		voice.stop();	// Stop immediately
	}
}
