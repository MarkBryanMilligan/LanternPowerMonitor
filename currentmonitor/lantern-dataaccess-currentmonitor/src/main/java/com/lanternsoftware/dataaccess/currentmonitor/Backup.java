package com.lanternsoftware.dataaccess.currentmonitor;

import com.lanternsoftware.datamodel.currentmonitor.Account;
import com.lanternsoftware.datamodel.currentmonitor.BreakerConfig;
import com.lanternsoftware.datamodel.currentmonitor.ChargeSummary;
import com.lanternsoftware.datamodel.currentmonitor.ChargeTotal;
import com.lanternsoftware.datamodel.currentmonitor.EnergySummary;
import com.lanternsoftware.datamodel.currentmonitor.EnergyTotal;
import com.lanternsoftware.datamodel.currentmonitor.Sequence;
import com.lanternsoftware.datamodel.rules.Event;
import com.lanternsoftware.datamodel.rules.FcmDevice;
import com.lanternsoftware.datamodel.rules.Rule;
import com.lanternsoftware.util.DebugTimer;
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.mongo.MongoConfig;

import java.util.List;

public class Backup {
	public static void main(String[] args) {
		CurrentMonitorDao dao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.BACKUP_SOURCE + "mongo.cfg"));
		CurrentMonitorDao backupDao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.BACKUP_DEST + "mongo.cfg"));

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
		for (Account a : accounts) {
			List<EnergySummary> energy = dao.getProxy().query(EnergySummary.class, new DaoQuery("account_id", a.getId()));
			DebugTimer t = new DebugTimer("Save Energy");
			backupDao.getProxy().save(energy);
			t.stop();
		}
		t5.stop();

		DebugTimer t6 = new DebugTimer("Query Energy Totals");
		for (Account a : accounts) {
			List<EnergyTotal> total = dao.getProxy().query(EnergyTotal.class, new DaoQuery("account_id", a.getId()));
			DebugTimer t = new DebugTimer("Save Summary");
			backupDao.getProxy().save(total);
			t.stop();
		}
		t6.stop();

		DebugTimer t7 = new DebugTimer("Query Charges");
		for (Account a : accounts) {
			List<ChargeSummary> charges = dao.getProxy().query(ChargeSummary.class, new DaoQuery("account_id", a.getId()));
			DebugTimer t = new DebugTimer("Save Charges");
			backupDao.getProxy().save(charges);
			t.stop();
		}
		t7.stop();

		DebugTimer t8 = new DebugTimer("Query Charge Totals");
		for (Account a : accounts) {
			List<ChargeTotal> charges = dao.getProxy().query(ChargeTotal.class, new DaoQuery("account_id", a.getId()));
			DebugTimer t = new DebugTimer("Save Charge Totals");
			backupDao.getProxy().save(charges);
			t.stop();
		}
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
