package com.lanternsoftware.dataaccess.currentmonitor;

import com.lanternsoftware.datamodel.currentmonitor.Account;
import com.lanternsoftware.datamodel.currentmonitor.BreakerConfig;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroupEnergy;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroupSummary;
import com.lanternsoftware.datamodel.currentmonitor.Sequence;
import com.lanternsoftware.datamodel.rules.Event;
import com.lanternsoftware.datamodel.rules.FcmDevice;
import com.lanternsoftware.datamodel.rules.Rule;
import com.lanternsoftware.util.DebugTimer;
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.dao.mongo.MongoConfig;

import java.util.List;

public class Backup {
	public static void main(String[] args) {
		CurrentMonitorDao dao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.OPS_PATH + "mongo.cfg"));
		CurrentMonitorDao backupDao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.BACKUP_PATH + "mongo.cfg"));

		DebugTimer t1 = new DebugTimer("Query Accounts");
		List<Account> accounts = dao.getProxy().queryAll(Account.class);
		t1.stop();
		DebugTimer t2 = new DebugTimer("Save Accounts");
		backupDao.getProxy().save(accounts);
		t2.stop();

		DebugTimer t3 = new DebugTimer("Query Configs");
		List<BreakerConfig> configs = dao.getProxy().queryAll(BreakerConfig.class);
		t3.stop();
		DebugTimer t4 = new DebugTimer("Save Configs");
		backupDao.getProxy().save(configs);
		t4.stop();

		DebugTimer t5 = new DebugTimer("Query Energy");
		List<BreakerGroupEnergy> energy = dao.getProxy().queryAll(BreakerGroupEnergy.class);
		t5.stop();
		DebugTimer t6 = new DebugTimer("Save Energy");
		backupDao.getProxy().save(energy);
		t6.stop();

		DebugTimer t7 = new DebugTimer("Query Summaries");
		List<BreakerGroupSummary> summary = dao.getProxy().queryAll(BreakerGroupSummary.class);
		t7.stop();
		DebugTimer t8 = new DebugTimer("Save Summaries");
		backupDao.getProxy().save(summary);
		t8.stop();

		DebugTimer t9 = new DebugTimer("Query Events");
		List<Event> events = dao.getProxy().queryAll(Event.class);
		t9.stop();
		DebugTimer t10 = new DebugTimer("Save Events");
		backupDao.getProxy().save(events);
		t10.stop();

		DebugTimer t11 = new DebugTimer("Query Devices");
		List<FcmDevice> devices = dao.getProxy().queryAll(FcmDevice.class);
		t11.stop();
		DebugTimer t12 = new DebugTimer("Save Devices");
		backupDao.getProxy().save(devices);
		t12.stop();

		DebugTimer t13 = new DebugTimer("Query Rules");
		List<Rule> rules = dao.getProxy().queryAll(Rule.class);
		t13.stop();
		DebugTimer t14 = new DebugTimer("Save Rules");
		backupDao.getProxy().save(rules);
		t14.stop();

		DebugTimer t15 = new DebugTimer("Query Sequences");
		List<Sequence> sequences = dao.getProxy().queryAll(Sequence.class);
		t15.stop();
		DebugTimer t16 = new DebugTimer("Save Sequences");
		backupDao.getProxy().save(sequences);
		t16.stop();

		dao.shutdown();
		backupDao.shutdown();
	}
}
