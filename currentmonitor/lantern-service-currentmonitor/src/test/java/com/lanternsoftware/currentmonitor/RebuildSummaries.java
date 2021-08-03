package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.dataaccess.currentmonitor.CurrentMonitorDao;
import com.lanternsoftware.dataaccess.currentmonitor.MongoCurrentMonitorDao;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.dao.mongo.MongoConfig;

import java.util.TimeZone;

public class RebuildSummaries {
	public static void main(String[] args) {
		CurrentMonitorDao dao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.OPS_PATH + "mongo.cfg"));
		TimeZone tz = TimeZone.getTimeZone("America/Chicago");
		dao.rebuildSummaries(100, DateUtils.date(8,3,2021, tz), DateUtils.date(8,5,2021, tz));
/*		List<Account> accounts = dao.getProxy().queryAll(Account.class);
		for (int accountId : CollectionUtils.transform(accounts, Account::getId)) {
			if (accountId != 100)
				continue;
			dao.rebuildSummaries(accountId, DateUtils.date(4,21,2021, tz), DateUtils.date(4,22,2021, tz));
		}
 */
		dao.shutdown();
	}
}
