package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.dataaccess.currentmonitor.CurrentMonitorDao;
import com.lanternsoftware.dataaccess.currentmonitor.MongoCurrentMonitorDao;
import com.lanternsoftware.datamodel.currentmonitor.Account;
import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.dao.mongo.MongoConfig;

public class CreateAccount {
	public static void main(String[] args) {
		CurrentMonitorDao dao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.CONFIG_PATH + "mongo.cfg"));
		Account account = new Account();
		account.setId(1);
		account.setPassword("*redacted*");

		account.setId(2);
		account.setUsername("admin@lanternsoftware.com");
		account.setPassword("*redacted*");

		dao.putAccount(account);
		dao.shutdown();
	}
}
