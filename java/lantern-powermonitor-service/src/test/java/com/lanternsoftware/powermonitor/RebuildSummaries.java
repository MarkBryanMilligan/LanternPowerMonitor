package com.lanternsoftware.powermonitor;

import com.lanternsoftware.powermonitor.dataaccess.PowerMonitorDao;
import com.lanternsoftware.powermonitor.dataaccess.MongoPowerMonitorDao;
import com.lanternsoftware.powermonitor.datamodel.Account;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.util.external.LanternFiles;

import java.util.TimeZone;

public class RebuildSummaries {
	public static void main(String[] args) {
		PowerMonitorDao dao = new MongoPowerMonitorDao(MongoConfig.fromDisk(LanternFiles.CONFIG_PATH + "mongo.cfg"));
		for (Account a : dao.getProxy().query(Account.class, null)) {
			TimeZone tz = DateUtils.fromTimeZoneId(a.getTimezone());
			dao.rebuildSummaries(a.getId(), DateUtils.date(11,12,2024,tz), DateUtils.date(11,14,2024,tz));
		}
		dao.shutdown();
	}
}
