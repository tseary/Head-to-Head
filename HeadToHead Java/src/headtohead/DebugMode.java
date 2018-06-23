package headtohead;

import java.io.File;

public final class DebugMode {
	static {
		File debugFile = new File("debug.txt");
		debugMode = debugFile.exists();
	}
	
	private DebugMode() {
		throw new AssertionError();
	}
	
	private static boolean debugMode = false;
	
	public static boolean isEnabled() {
		return debugMode;
	}
}
