package com.lanternsoftware.util.external;

public abstract class LanternFiles {
	public static String SOURCE_CODE_PATH;
	public static String CONFIG_PATH;
	public static String BACKUP_DEST_PATH;
	public static boolean runOpsTasks;

	static {
		ProdConsoleFiles.init();
	}
}
