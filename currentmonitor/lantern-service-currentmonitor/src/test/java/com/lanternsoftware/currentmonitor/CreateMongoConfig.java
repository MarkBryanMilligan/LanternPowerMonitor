package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.dao.mongo.MongoConfig;

public class CreateMongoConfig {
	public static void main(String[] args) {
		new MongoConfig("lanternsoftware.com", "*redacted*", "*redacted*", "CURRENT_MONITOR").saveToDisk(LanternFiles.CONFIG_PATH + "mongo.cfg");
	}
}
