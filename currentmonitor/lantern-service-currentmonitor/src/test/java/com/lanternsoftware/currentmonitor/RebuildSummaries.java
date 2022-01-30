package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.dataaccess.currentmonitor.CurrentMonitorDao;
import com.lanternsoftware.dataaccess.currentmonitor.MongoCurrentMonitorDao;
import com.lanternsoftware.datamodel.currentmonitor.Account;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.dao.mongo.MongoConfig;

import java.util.TimeZone;

public class RebuildSummaries {
	public static void main(String[] args) {
		CurrentMonitorDao dao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.CONFIG_PATH + "mongo.cfg"));
		for (Account a : dao.getProxy().queryAll(Account.class)) {
			TimeZone tz = DateUtils.fromTimeZoneId(a.getTimezone());
//			BreakerConfig config = dao.getConfig(a.getId());
//			BillingPlan plan = new BillingPlan();
//			plan.setPlanId(1);
//			plan.setBillingDay(1);
//			plan.setName("Standard");
//			plan.setRates(config.getBillingRates());
//			config.setBillingPlans(CollectionUtils.asArrayList(plan));
//			dao.putConfig(config);
			dao.rebuildSummaries(a.getId(), DateUtils.date(12,18,2021,tz), DateUtils.date(12,19,2021,tz));
		}
		dao.shutdown();
	}
}
