package sound;

public enum SoundName {
	BOUNCE,
	BUMP,
	CRACK,
	CRASH,
	ENGINE_IDLE(3),
	ENGINE_1(3),
	ENGINE_2(3),
	ENGINE_3(3),
	EXPLODE,
	GAMEOVER(1),
	GAMEOVER_P0(1),
	GAMEOVER_P1(1),
	HIT,
	PEW,
	PWANK_C,
	PWANK_E;
	
	/**
	 * TYPE_SINGLE - Only one copy of this sound is expected to play concurrently.
	 * TYPE_MULTI - Multiple copies of this sound are expected to play concurrently.
	 * TYPE_LOOPING - Only one copy of this sound plays concurrently, and loops until it is stopped.
	 */
	public final static int TYPE_SINGLE = 1,
			TYPE_MULTI = 2,
			TYPE_LOOPING = 3;
	
	private int type;
	
	private SoundName() {
		this.type = TYPE_MULTI;	// Default type
	}
	
	private SoundName(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
}
