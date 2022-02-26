package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.util.external.LanternFiles;

public class CreateMongoConfig {
	public static void main(String[] args) {
		if (CollectionUtils.size(args) == 3)
			new MongoConfig(args[0], args[1], args[2], "CURRENT_MONITOR").saveToDisk(LanternFiles.CONFIG_PATH + "mongo.cfg");
		else
			new MongoConfig("lanternsoftware.com", "*redacted*", "*redacted*", "CURRENT_MONITOR").saveToDisk(LanternFiles.CONFIG_PATH + "mongo.cfg");
	}
}
