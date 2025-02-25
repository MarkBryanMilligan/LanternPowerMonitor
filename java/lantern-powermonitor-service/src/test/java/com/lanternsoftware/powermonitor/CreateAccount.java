package com.lanternsoftware.powermonitor;

import com.lanternsoftware.powermonitor.dataaccess.PowerMonitorDao;
import com.lanternsoftware.powermonitor.dataaccess.MongoPowerMonitorDao;
import com.lanternsoftware.powermonitor.datamodel.BreakerConfig;
import com.lanternsoftware.powermonitor.datamodel.HubCommand;
import com.lanternsoftware.powermonitor.datamodel.HubConfigCharacteristic;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.util.external.LanternFiles;

public class CreateAccount {
	public static void main(String[] args) {
		PowerMonitorDao dao = new MongoPowerMonitorDao(MongoConfig.fromDisk(LanternFiles.CONFIG_PATH + "mongo.cfg"));

		//		BreakerConfig config = dao.getConfig(856);
//		List<Breaker> breakers = CollectionUtils.filter(config.getAllBreakers(), _b->_b.getPolarity() == BreakerPolarity.SOLAR);
//		Set<Integer> keys = CollectionUtils.transformToSet(breakers, Breaker::getIntKey);
//		List<HubPowerMinute> minutes = dao.getProxy().query(HubPowerMinute.class, new DaoQuery("account_id", 856));
//		for (HubPowerMinute minute : minutes) {
//			for (BreakerPowerMinute breaker : CollectionUtils.filter(minute.getBreakers(), _b->keys.contains(_b.breakerIntKey()))) {
//				List<Float> doubled = new ArrayList<>(breaker.getReadings().size());
//				for (Float reading : breaker.getReadings()) {
//					doubled.add(reading*2);
//				}
//				breaker.setReadings(doubled);
//			}
//			dao.getProxy().save(minute);
//		}
//		dao.rebuildSummaries(856);

//		Account account = new Account();
//		account.setId(1);
//		account.setPassword("*redacted*");
//
//		account.setId(2);
//		account.setUsername("admin@lanternsoftware.com");
//		account.setPassword("*redacted*");
//
//		dao.putAccount(account);


//		HubCommand c = new HubCommand();
//		c.setAccountId(550);
//		c.setCharacteristic(HubConfigCharacteristic.Restart);
//		c.setHub(1);
//		dao.putHubCommand(c);


//		BreakerConfig config = dao.getConfig(400);
//		List<Breaker> monitored = CollectionUtils.filter(config.getAllBreakers(), _b->_b.getPort() > 0);

//		BreakerConfig config = dao.getConfig(421);
//		CollectionUtils.edit(config.getBreakerHubs(), _b->_b.setPhaseOffsetNs(0));
//		BreakerHub hub = new BreakerHub();
//		hub.setHub(1);
//		config.setBreakerHubs(CollectionUtils.asArrayList(hub));
//		dao.putConfig(config);

//		List<Account> accounts = dao.getProxy().query(Account.class, DaoQuery.in("timezone", CollectionUtils.asArrayList("Asia/Tehran","Asia/Kabul","Asia/Ashgabat", "Asia/Dubai")));
//
//		for (Account acct : accounts) {
//			dao.deleteAccount(acct.getId());
//		}



//		List<BreakerConfig> configs = dao.getProxy().query(BreakerConfig.class, DaoQuery.in("_id", CollectionUtils.transform(accounts, _a->String.valueOf(_a.getId()))));
//		List<BreakerConfig> configsWithBreakers = CollectionUtils.filter(configs, _c->!_c.getAllBreakers().isEmpty());
//		List<Integer> validAccounts = CollectionUtils.transform(configsWithBreakers, BreakerConfig::getAccountId);
//		List<EnergySummary> summaries = dao.getProxy().query(EnergySummary.class, DaoQuery.inIntegers("account_id", validAccounts));
//		Set<Integer> activeAccountIds = CollectionUtils.transformToSet(summaries, EnergySummary::getAccountId);
//		List<Account> activeAccounts = CollectionUtils.filter(accounts, _a->activeAccountIds.contains(_a.getId()));
		BreakerConfig config = dao.getConfig(998);
//		List<Breaker> breakers = CollectionUtils.filter(config.getAllBreakers(), _b->_b.getPolarity() == BreakerPolarity.SOLAR);
//		CollectionUtils.edit(breakers, _b->_b.setPolarity(BreakerPolarity.BI_DIRECTIONAL_INVERTED));
//		dao.putConfig(config);
		dao.putHubCommand(new HubCommand(config.getAccountId(), HubConfigCharacteristic.ReloadConfig, null));

		dao.shutdown();
	}
}
