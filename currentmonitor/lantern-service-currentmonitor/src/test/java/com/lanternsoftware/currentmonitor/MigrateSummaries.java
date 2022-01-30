package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.dataaccess.currentmonitor.CurrentMonitorDao;
import com.lanternsoftware.dataaccess.currentmonitor.MongoCurrentMonitorDao;
import com.lanternsoftware.datamodel.currentmonitor.EnergySummary;
import com.lanternsoftware.datamodel.currentmonitor.EnergyTotal;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.dao.mongo.MongoConfig;

public class MigrateSummaries {
	public static void main(String[] args) {
		CurrentMonitorDao dao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.CONFIG_PATH + "mongo.cfg"));
//		TimeZone tz = TimeZone.getTimeZone("America/Chicago");
//		List<BreakerGroupEnergy> summaries = dao.getProxy().query(BreakerGroupEnergy.class, null);
//		CollectionUtils.edit(summaries, _s->CollectionUtils.edit(_s.getAllGroups(), _t->_t.setAccountId(1)));
//		dao.getProxy().save(summaries);

		dao.getProxy().save(CollectionUtils.transform(dao.getProxy().queryAll(EnergySummary.class), EnergyTotal::new));

//		List<BreakerPower> readings = null;
//		while ((readings == null) || !readings.isEmpty()) {
//			readings = dao.getProxy().query(BreakerPower.class, new DaoQuery("account_id", new DaoQuery("$ne", 1)), null, null, 0, 1000000);
//			System.out.println("Adding account id to " + readings.size() + " power readings");
//			CollectionUtils.edit(readings, _s -> _s.setAccountId(1));
//			dao.getProxy().save(readings);
//		}
//
//		List<BreakerPowerArchive> archives = null;
//		while ((archives == null) || !archives.isEmpty()) {
//			archives = dao.getProxy().query(BreakerPowerArchive.class, new DaoQuery("account_id", new DaoQuery("$ne", 1)), null, null, 0, 50);
//			System.out.println("Adding account id to " + archives.size() + " archives");
//			CollectionUtils.edit(archives, _s -> _s.setAccountId(1));
//			dao.getProxy().save(archives);
//		}

//		List<BreakerPower> readings = CollectionUtils.filter(dao.getBreakerPower(Arrays.asList("0-1", "0-2"), DateUtils.date(6,26,2020, 17, 0, 0, 0, tz), DateUtils.date(6,26,2020, 22, 0, 0, 0, tz)), _p->_p.getPower() > 0.0);
//		CollectionUtils.edit(readings, _p->_p.setPower(-_p.getPower()));
//		dao.getProxy().save(readings);

//		Map<String, List<BreakerPower>> dups = CollectionUtils.transformToMultiMap(dao.getBreakerPower(Arrays.asList("2-1","2-2","2-3","2-4","2-5","2-6","2-7","2-8","2-9","2-10","2-11","2-12","2-13","2-14","2-15"), DateUtils.date(6,26,2020, 17, 0, 0, 0, tz), DateUtils.date(6,26,2020, 18, 0, 0, 0, tz)), _p->_p.getKey()+_p.getReadTime().getTime());
//		for (List<BreakerPower> dup : dups.values()) {
//			if (dup.size() > 1) {
//				CollectionUtils.removeFirst(dup);
//				dao.getProxy().delete(BreakerPower.class, DaoQuery.in("_id", CollectionUtils.transform(dup, BreakerPower::getId)));
//			}
//		}

//		List<BreakerGroupEnergy> summaries = dao.getProxy().query(BreakerGroupEnergy.class, null);
//		ResourceLoader.writeFile(LanternFiles.OPS_PATH + "summaryBackup.json", DaoSerializer.toJson(DaoSerializer.toDaoEntities(summaries)));
//		for (BreakerGroupEnergy summary : summaries) {
//			dao.getProxy().save(summary);
//		}
	}
}
