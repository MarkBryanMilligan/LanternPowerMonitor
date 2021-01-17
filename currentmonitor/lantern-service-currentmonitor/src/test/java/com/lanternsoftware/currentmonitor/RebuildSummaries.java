package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.dataaccess.currentmonitor.CurrentMonitorDao;
import com.lanternsoftware.dataaccess.currentmonitor.MongoCurrentMonitorDao;
import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.datamodel.currentmonitor.BreakerConfig;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroup;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroupEnergy;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroupSummary;
import com.lanternsoftware.datamodel.currentmonitor.EnergyBlockViewMode;
import com.lanternsoftware.datamodel.currentmonitor.HubPowerMinute;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.DebugTimer;
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.mongo.MongoConfig;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class RebuildSummaries {
	public static void main(String[] args) {
		int accountId = 1;
		CurrentMonitorDao dao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.OPS_PATH + "mongo.cfg"));
		TimeZone tz = dao.getTimeZoneForAccount(accountId);
		Date start = DateUtils.date(1, 7, 2021, tz);
//		Date start = DateUtils.getMidnightBeforeNow(tz);
		Date end = DateUtils.getMidnightAfterNow(tz);
		Map<Date, List<HubPowerMinute>> days = CollectionUtils.transformToMultiMap(dao.getProxy().query(HubPowerMinute.class, new DaoQuery("account_id", accountId).andBetweenInclusiveExclusive("minute", (int)(start.getTime()/60000), (int)(end.getTime()/60000))), _m->DateUtils.getMidnightBefore(_m.getMinuteAsDate(), tz));
		BreakerConfig config = dao.getConfig(accountId);
		BreakerGroup root = CollectionUtils.getFirst(config.getBreakerGroups());
		Map<String, Breaker> breakers = CollectionUtils.transformToMap(root.getAllBreakers(), Breaker::getKey);
		Map<String, BreakerGroup> breakerKeyToGroup = new HashMap<>();
		for (BreakerGroup group : root.getAllBreakerGroups()) {
			for (Breaker b : group.getAllBreakers()) {
				breakerKeyToGroup.put(b.getKey(), group);
			}
		}

		for (Map.Entry<Date, List<HubPowerMinute>> day : days.entrySet()) {
			BreakerGroupEnergy energy = null;
			DebugTimer timer = new DebugTimer("Time to rebuild one day");
			Map<Integer, List<HubPowerMinute>> minutes = CollectionUtils.transformToMultiMap(day.getValue(), HubPowerMinute::getMinute);
			for (List<HubPowerMinute> minute : minutes.values()) {
				if (energy == null)
					energy = new BreakerGroupEnergy(root, minute, EnergyBlockViewMode.DAY, day.getKey(), tz);
				else
					energy.addEnergy(breakers, breakerKeyToGroup, minute);
			}
			timer.stop();
			if (energy != null)
				dao.putBreakerGroupEnergy(energy);
		}
		dao.updateSummaries(root, days.keySet(), tz);

//		List<BreakerGroupEnergy> summaries = dao.getProxy().query(BreakerGroupEnergy.class, null);
//		CollectionUtils.edit(summaries, _s->CollectionUtils.edit(_s.getAllGroups(), _t->_t.setAccountId(1)));
//		dao.getProxy().save(summaries);

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
		dao.shutdown();
	}
}
