package headtohead;

import java.io.File;

public final class DebugMode {
	private DebugMode() {
		throw new AssertionError();
	}
	
	private static boolean debugMode = false;
	
	static {
		File debugFile = new File("debug.txt");
		debugMode = debugFile.exists();
	}
	
	public static boolean isEnabled() {
		return debugMode;
	}
}
