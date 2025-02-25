package com.lanternsoftware.powermonitor.dataaccess;

import com.lanternsoftware.powermonitor.datamodel.Account;
import com.lanternsoftware.powermonitor.datamodel.BreakerConfig;
import com.lanternsoftware.powermonitor.datamodel.ChargeSummary;
import com.lanternsoftware.powermonitor.datamodel.ChargeTotal;
import com.lanternsoftware.powermonitor.datamodel.EnergySummary;
import com.lanternsoftware.powermonitor.datamodel.EnergyTotal;
import com.lanternsoftware.powermonitor.datamodel.Sequence;
import com.lanternsoftware.datamodel.rules.Event;
import com.lanternsoftware.datamodel.rules.FcmDevice;
import com.lanternsoftware.datamodel.rules.Rule;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.DebugTimer;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.util.external.LanternFiles;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Backup {
	public static void main(String[] args) {
		PowerMonitorDao dao = new MongoPowerMonitorDao(MongoConfig.fromDisk(LanternFiles.CONFIG_PATH + "mongo.cfg"));
		PowerMonitorDao backupDao = new MongoPowerMonitorDao(MongoConfig.fromDisk(LanternFiles.BACKUP_DEST_PATH + "mongo.cfg"));
		Date start = DateUtils.date(12,31,2023, TimeZone.getTimeZone("UTC")); //08/04/2023

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
			List<EnergySummary> energy = dao.getProxy().query(EnergySummary.class, new DaoQuery("account_id", a.getId()).andGte("start", start.getTime()));
			DebugTimer t = new DebugTimer("Save Energy");
			backupDao.getProxy().save(energy);
			t.stop();
		}
		t5.stop();

		DebugTimer t6 = new DebugTimer("Query Energy Totals");
		for (Account a : accounts) {
			List<EnergyTotal> total = dao.getProxy().query(EnergyTotal.class, new DaoQuery("account_id", a.getId()).andGte("start", start.getTime()));
			DebugTimer t = new DebugTimer("Save Summary");
			backupDao.getProxy().save(total);
			t.stop();
		}
		t6.stop();

		DebugTimer t7 = new DebugTimer("Query Charges");
		for (Account a : accounts) {
			List<ChargeSummary> charges = dao.getProxy().query(ChargeSummary.class, new DaoQuery("account_id", a.getId()).andGte("start", start.getTime()));
			DebugTimer t = new DebugTimer("Save Charges");
			backupDao.getProxy().save(charges);
			t.stop();
		}
		t7.stop();

		DebugTimer t8 = new DebugTimer("Query Charge Totals");
		for (Account a : accounts) {
			List<ChargeTotal> charges = dao.getProxy().query(ChargeTotal.class, new DaoQuery("account_id", a.getId()).andGte("start", start.getTime()));
			DebugTimer t = new DebugTimer("Save Charge Totals");
			backupDao.getProxy().save(charges);
			t.stop();
		}
		t8.stop();

		DebugTimer t9 = new DebugTimer("Query Events");
		List<Event> events = dao.getProxy().query(Event.class, DaoQuery.gte("time", start.getTime()));
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
