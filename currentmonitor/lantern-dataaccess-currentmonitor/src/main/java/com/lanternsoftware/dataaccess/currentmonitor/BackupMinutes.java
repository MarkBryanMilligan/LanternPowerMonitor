package com.lanternsoftware.dataaccess.currentmonitor;

import com.lanternsoftware.datamodel.currentmonitor.Account;
import com.lanternsoftware.datamodel.currentmonitor.HubPowerMinute;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.DebugTimer;
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.DaoSort;
import com.lanternsoftware.util.dao.mongo.MongoConfig;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class BackupMinutes {
	public static void main(String[] args) {
		CurrentMonitorDao dao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.BACKUP_SOURCE + "mongo.cfg"));
		CurrentMonitorDao backupDao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.BACKUP_DEST + "mongo.cfg"));
		Date now = new Date();
		for (Account a : dao.getProxy().queryAll(Account.class)) {
			if (a.getId() == 0)
				continue;
			DebugTimer t = new DebugTimer("Account " + a.getId());
			if (NullUtils.isEmpty(a.getTimezone())) {
				a.setTimezone("America/Chicago");
			}
			TimeZone tz = TimeZone.getTimeZone(a.getTimezone());
			HubPowerMinute minute = dao.getProxy().queryOne(HubPowerMinute.class, new DaoQuery("account_id", a.getId()), DaoSort.sort("minute"));
			if (minute == null)
				continue;
			HubPowerMinute lastBackup = backupDao.getProxy().queryOne(HubPowerMinute.class, new DaoQuery("account_id", a.getId()), DaoSort.sortDesc("minute"));
			Date start = lastBackup == null ? DateUtils.getMidnightBefore(minute.getMinuteAsDate(), tz) : lastBackup.getMinuteAsDate();
//			Date start = DateUtils.date(10,16,2021,tz);
			Date end = DateUtils.addDays(start, 1, tz);
			while (start.before(now)) {
				DebugTimer t2 = new DebugTimer("Account Id: " + a.getId() + " Query Day " + DateUtils.format("MM/dd/yyyy", tz, start));
				List<HubPowerMinute> minutes = dao.getProxy().query(HubPowerMinute.class, new DaoQuery("account_id", a.getId()).andBetweenInclusiveExclusive("minute", (int) (start.getTime() / 60000), (int) (end.getTime() / 60000)));
				t2.stop();
				if (!minutes.isEmpty()) {
					DebugTimer t3 = new DebugTimer("Save Day");
					backupDao.getProxy().save(minutes);
					t3.stop();
				}
				start = end;
				end = DateUtils.addDays(end, 1, tz);
			}
			t.stop();
		}
		dao.shutdown();
		backupDao.shutdown();
	}
}
