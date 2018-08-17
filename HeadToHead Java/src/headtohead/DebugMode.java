package headtohead;

import java.io.File;

public final class DebugMode {
	private DebugMode() {
		throw new AssertionError();
	}
	
	/** Set this to true to enable debug behaviour. */
	private static boolean debugMode = true;
	
	static {
		File debugFile = new File("debug.txt");
		debugMode = debugFile.exists();
	}
	
	public static boolean isEnabled() {
		return debugMode;
	}
}
