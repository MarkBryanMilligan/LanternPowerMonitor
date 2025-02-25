package com.lanternsoftware.powermonitor;

import com.lanternsoftware.powermonitor.dataaccess.PowerMonitorDao;
import com.lanternsoftware.powermonitor.dataaccess.MongoPowerMonitorDao;
import com.lanternsoftware.powermonitor.datamodel.Account;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.util.external.LanternFiles;

import java.util.Arrays;
import java.util.List;

public class InvalidAccounts {
	public static void main(String[] args) {
		PowerMonitorDao dao = new MongoPowerMonitorDao(MongoConfig.fromDisk(LanternFiles.CONFIG_PATH + "mongo.cfg"));
//		List<Account> accounts = dao.getProxy().queryAll(Account.class);
//		Set<Integer> validAccountIds = new TreeSet<>(CollectionUtils.transform(dao.getProxy().queryForField(BreakerPower.class, new DaoQuery(), "account_id"), NullUtils::toInteger));
//		List<Account> invalidAccounts = CollectionUtils.filter(accounts, _a->!validAccountIds.contains(_a.getId()));
//		List<Account> validAccounts = CollectionUtils.filter(accounts, _a->validAccountIds.contains(_a.getId()));
//		System.out.println(new TreeSet<>(CollectionUtils.transform(invalidAccounts, _a-> NullUtils.makeNotNull(_a.getTimezone()))));
//		System.out.println(new TreeSet<>(CollectionUtils.transform(validAccounts, _a-> NullUtils.makeNotNull(_a.getTimezone()))));
		List<String> banned = Arrays.asList("Asia/Aden",
				"Asia/Baghdad",
				"Asia/Baku",
				"Asia/Dubai",
				"Asia/Novosibirsk",
				"Asia/Omsk",
				"Asia/Riyadh",
				"Asia/Tehran",
				"Asia/Vladivostok",
				"Asia/Yekaterinburg",
				"Europe/Minsk",
				"Europe/Moscow");
		List<Account> acct = dao.getProxy().query(Account.class, DaoQuery.in("timezone", banned));
		for (Account a : acct) {
			System.out.println(a.getId() + " - " + a.getUsername());
		}
//		for (String tz : banned) {
//			dao.getProxy().saveEntity("tz_blacklist", new DaoEntity("tz", tz));
//		}
		dao.shutdown();
	}
}
