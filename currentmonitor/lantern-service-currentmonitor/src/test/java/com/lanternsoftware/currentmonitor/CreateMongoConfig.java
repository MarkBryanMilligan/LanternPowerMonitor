package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.dao.mongo.MongoConfig;

public class CreateMongoConfig {
	public static void main(String[] args) {
		new MongoConfig("localhost", "*redacted*", "*redacted*", "CURRENT_MONITOR").saveToDisk(LanternFiles.OPS_PATH + "mongo.cfg");
	}
}
