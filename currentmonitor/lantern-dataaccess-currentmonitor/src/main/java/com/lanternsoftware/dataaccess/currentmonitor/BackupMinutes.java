package com.lanternsoftware.dataaccess.currentmonitor;

import com.lanternsoftware.datamodel.currentmonitor.Account;
import com.lanternsoftware.datamodel.currentmonitor.HubPowerMinute;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.DebugTimer;
import com.lanternsoftware.util.concurrency.ConcurrencyUtils;
import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.DaoSort;
import com.lanternsoftware.util.dao.mongo.MongoConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BackupMinutes {


	public static void main(String[] args) {
		CurrentMonitorDao sourceDao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.CONFIG_PATH + "mongo.cfg"));
		CurrentMonitorDao destDao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.BACKUP_DEST_PATH + "mongo.cfg"));
		ExecutorService ex = Executors.newFixedThreadPool(8);
		List<Future<?>> tasks = new ArrayList<>();
		for (Account a : sourceDao.getProxy().queryAll(Account.class)) {
			if (a.getId() == 0)
				continue;
			DebugTimer t = new DebugTimer("Account " + a.getId());
			if (NullUtils.isEmpty(a.getTimezone())) {
				a.setTimezone("America/Chicago");
			}
			TimeZone tz = TimeZone.getTimeZone(a.getTimezone());
			HubPowerMinute firstMinute = sourceDao.getProxy().queryOne(HubPowerMinute.class, new DaoQuery("account_id", a.getId()), DaoSort.sort("minute"));
			if (firstMinute == null)
				continue;
			HubPowerMinute lastMinute = sourceDao.getProxy().queryOne(HubPowerMinute.class, new DaoQuery("account_id", a.getId()), DaoSort.sortDesc("minute"));
			HubPowerMinute lastBackup = destDao.getProxy().queryOne(HubPowerMinute.class, new DaoQuery("account_id", a.getId()), DaoSort.sortDesc("minute"));
			Date start = lastBackup == null ? DateUtils.getMidnightBefore(firstMinute.getMinuteAsDate(), tz) : lastBackup.getMinuteAsDate();
			Date lastMin = lastMinute.getMinuteAsDate();
			Date end = DateUtils.addDays(start, 1, tz);
			while (start.before(lastMin)) {
				final Date curStart = start;
				final Date curEnd = end;
				tasks.add(ex.submit(() -> {
					DebugTimer t2 = new DebugTimer("Account Id: " + a.getId() + " Query Day " + DateUtils.format("MM/dd/yyyy", tz, curStart));
					List<HubPowerMinute> minutes = sourceDao.getProxy().query(HubPowerMinute.class, new DaoQuery("account_id", a.getId()).andBetweenInclusiveExclusive("minute", (int) (curStart.getTime() / 60000), (int) (curEnd.getTime() / 60000)));
					t2.stop();
					if (!minutes.isEmpty()) {
						DebugTimer t3 = new DebugTimer("Save Day");
						destDao.getProxy().save(minutes);
						t3.stop();
					}
				}));
				start = end;
				end = DateUtils.addDays(end, 1, tz);
			}
			t.stop();
		}
		ConcurrencyUtils.getAll(tasks);
		ex.shutdown();
		sourceDao.shutdown();
		destDao.shutdown();
	}
}
