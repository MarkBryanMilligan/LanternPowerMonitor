package com.lanternsoftware.dataaccess.currentmonitor;

import com.lanternsoftware.datamodel.currentmonitor.Account;
import com.lanternsoftware.datamodel.currentmonitor.BillingRate;
import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.datamodel.currentmonitor.BreakerConfig;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroup;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroupEnergy;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroupSummary;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPower;
import com.lanternsoftware.datamodel.currentmonitor.EnergyBlockViewMode;
import com.lanternsoftware.datamodel.currentmonitor.HubPowerMinute;
import com.lanternsoftware.datamodel.currentmonitor.Sequence;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.DebugTimer;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.cryptography.AESTool;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.DaoSort;
import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.util.dao.mongo.MongoProxy;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MongoCurrentMonitorDao implements CurrentMonitorDao {
	private static final Logger logger = LoggerFactory.getLogger(MongoCurrentMonitorDao.class);
	private static final AESTool aes = AESTool.authTool();
	private static final int BCRYPT_ROUNDS = 11;
	private final Timer delayTimer = new Timer();
	private final ExecutorService executor = Executors.newCachedThreadPool();

	private final MongoProxy proxy;

	public MongoCurrentMonitorDao(MongoConfig _config) {
		proxy = new MongoProxy(_config);
		proxy.ensureIndex(BreakerPower.class, DaoSort.sort("account_id").then("key"));
		proxy.ensureIndex(HubPowerMinute.class, DaoSort.sort("account_id").then("minute"));
		proxy.ensureIndex(BreakerGroupEnergy.class, DaoSort.sort("account_id").then("group_id").then("view_mode"));
		proxy.ensureIndex(BreakerGroupSummary.class, DaoSort.sort("account_id").then("group_id").then("view_mode").then("start"));
		proxy.ensureIndex(DirtyMinute.class, DaoSort.sort("posted"));
		for (DirtyMinute minute : proxy.queryAll(DirtyMinute.class)) {
			updateSummaries(minute);
		}
		proxy.delete(DirtyMinute.class, new DaoQuery());
	}

	public void shutdown() {
		delayTimer.cancel();
		executor.shutdownNow();
		proxy.shutdown();
	}

	@Override
	public void putBreakerPower(BreakerPower _power) {
		proxy.save(_power);
	}

	@Override
	public void putHubPowerMinute(HubPowerMinute _power) {
		if (_power == null)
			return;
		proxy.save(_power);
		DirtyMinute minute = new DirtyMinute(_power.getAccountId(), _power.getMinute(), new Date());
		proxy.save(minute);
		delayTimer.schedule(new TimerTask(){
			@Override
			public void run() {
				executor.submit(()->{
					if (proxy.queryOneAndDelete(DirtyMinute.class, new DaoQuery("_id", minute.getId())) != null)
						updateSummaries(new DirtyMinute(_power.getAccountId(), _power.getMinute(), new Date()));
				});
			}
		}, 10000);
	}

	private void updateSummaries(DirtyMinute _minute) {
		DebugTimer timer = new DebugTimer("Updating summaries", logger);
		List<HubPowerMinute> minutes = proxy.query(HubPowerMinute.class, new DaoQuery("account_id", _minute.getAccountId()).and("minute", _minute.getMinute()));
		TimeZone tz = getTimeZoneForAccount(_minute.getAccountId());
		BreakerConfig config = getConfig(_minute.getAccountId());
		BreakerGroup group = CollectionUtils.getFirst(config.getBreakerGroups());
		Date day = DateUtils.getMidnightBefore(_minute.getMinuteAsDate(), tz);
		BreakerGroupEnergy energy = getBreakerGroupEnergy(_minute.getAccountId(), group.getId(), EnergyBlockViewMode.DAY, day);
		Date monthStart = DateUtils.getStartOfMonth(day, tz);
		BreakerGroupSummary month = proxy.queryOne(BreakerGroupSummary.class, new DaoQuery("_id", BreakerGroupEnergy.toId(_minute.getAccountId(), group.getId(), EnergyBlockViewMode.MONTH, monthStart)));
		if (energy == null)
			energy = new BreakerGroupEnergy(group, minutes, EnergyBlockViewMode.DAY, day, month, config.getBillingRates(), tz);
		else
			energy.addEnergy(group, minutes, month, config.getBillingRates());
		putBreakerGroupEnergy(energy);
		updateSummaries(group, CollectionUtils.asHashSet(day), tz);
		timer.stop();
	}

	@Override
	public List<BreakerPower> getBreakerPowerForAccount(int _accountId) {
		return proxy.query(BreakerPower.class, new DaoQuery("account_id", _accountId).andGt("read_time", DateUtils.minutesFromNow(-1).getTime()));
	}

	@Override
	public BreakerPower getLatestBreakerPower(int _accountId, int _panel, int _space) {
		return proxy.queryOne(BreakerPower.class, new DaoQuery("account_id", _accountId).and("key", Breaker.key(_panel, _space)), DaoSort.sortDesc("read_time"));
	}

	@Override
	public BreakerGroupEnergy getBreakerGroupEnergy(int _accountId, String _groupId, EnergyBlockViewMode _viewMode, Date _start) {
		return proxy.queryOne(BreakerGroupEnergy.class, new DaoQuery("_id", BreakerGroupEnergy.toId(_accountId, _groupId, _viewMode, _start)));
	}

	@Override
	public byte[] getBreakerGroupEnergyBinary(int _accountId, String _groupId, EnergyBlockViewMode _viewMode, Date _start) {
		return DaoSerializer.toZipBson(proxy.queryForEntity(BreakerGroupEnergy.class, new DaoQuery("_id", BreakerGroupEnergy.toId(_accountId, _groupId, _viewMode, _start))));
	}

	@Override
	public void updateSummaries(BreakerGroup _rootGroup, Set<Date> _daysToSummarize, TimeZone _tz) {
		Set<Date> monthsToSummarize = CollectionUtils.transformToSet(_daysToSummarize, _c -> DateUtils.getStartOfMonth(_c, _tz));
		Set<Date> yearsToSummarize = CollectionUtils.transformToSet(monthsToSummarize, _c -> DateUtils.getStartOfYear(_c, _tz));
		for (Date month : monthsToSummarize) {
			Calendar calDayStart = DateUtils.toCalendar(month, _tz);
			Calendar end = DateUtils.getEndOfMonthCal(month, _tz);
			List<String> groupEnergyIds = new ArrayList<>();
			while (calDayStart.before(end)) {
				groupEnergyIds.add(BreakerGroupEnergy.toId(_rootGroup.getAccountId(), _rootGroup.getId(), EnergyBlockViewMode.DAY, calDayStart.getTime()));
				calDayStart.add(Calendar.DAY_OF_YEAR, 1);
			}
			List<BreakerGroupSummary> groupEnergies = CollectionUtils.aggregate(proxy.query(BreakerGroupSummary.class, DaoQuery.in("_id", groupEnergyIds)), BreakerGroupSummary::getAllGroups);
			Map<String, List<BreakerGroupSummary>> energies = CollectionUtils.transformToMultiMap(groupEnergies, BreakerGroupSummary::getGroupId);
			BreakerGroupEnergy summary = BreakerGroupEnergy.summary(_rootGroup, energies, EnergyBlockViewMode.MONTH, month, _tz);
			putBreakerGroupEnergy(summary);
		}
		for (Date year : yearsToSummarize) {
			Calendar calMonthStart = DateUtils.toCalendar(year, _tz);
			Calendar end = DateUtils.getEndOfYearCal(year, _tz);
			List<String> groupEnergyIds = new ArrayList<>();
			while (calMonthStart.before(end)) {
				groupEnergyIds.add(BreakerGroupEnergy.toId(_rootGroup.getAccountId(), _rootGroup.getId(), EnergyBlockViewMode.MONTH, calMonthStart.getTime()));
				calMonthStart.add(Calendar.MONTH, 1);
			}
			List<BreakerGroupSummary> groupEnergies = CollectionUtils.aggregate(proxy.query(BreakerGroupSummary.class, DaoQuery.in("_id", groupEnergyIds)), BreakerGroupSummary::getAllGroups);
			Map<String, List<BreakerGroupSummary>> energies = CollectionUtils.transformToMultiMap(groupEnergies, BreakerGroupSummary::getGroupId);
			BreakerGroupEnergy summary = BreakerGroupEnergy.summary(_rootGroup, energies, EnergyBlockViewMode.YEAR, year, _tz);
			putBreakerGroupEnergy(summary);
		}
		List<BreakerGroupSummary> groupEnergies = CollectionUtils.aggregate(proxy.query(BreakerGroupSummary.class, new DaoQuery("account_id", _rootGroup.getAccountId()).and("group_id", _rootGroup.getId()).and("view_mode", EnergyBlockViewMode.YEAR.name())), BreakerGroupSummary::getAllGroups);
		Map<String, List<BreakerGroupSummary>> energies = CollectionUtils.transformToMultiMap(groupEnergies, BreakerGroupSummary::getGroupId);
		BreakerGroupEnergy summary = BreakerGroupEnergy.summary(_rootGroup, energies, EnergyBlockViewMode.ALL, new Date(0), _tz);
		putBreakerGroupEnergy(summary);
	}

	private void rebuildSummaries(int _accountId, Collection<BillingRate> _rates) {
		logger.info("Rebuilding summaries due to a change in {} rates", CollectionUtils.size(_rates));
		HubPowerMinute firstMinute = proxy.queryOne(HubPowerMinute.class, new DaoQuery("account_id", _accountId), DaoSort.sort("minute"));
		if (firstMinute == null)
			return;
		TimeZone tz = getTimeZoneForAccount(_accountId);
		Map<String, BillingRate> rates = CollectionUtils.transformToMap(_rates, _r->String.format("%d%d", DaoSerializer.toLong(_r.getBeginEffective()), DaoSerializer.toLong(_r.getEndEffective())));
		for (BillingRate rate : rates.values()) {
			Date start = rate.getBeginEffective();
			Date end = rate.getEndEffective();
			Date now = new Date();
			if ((start == null) || start.before(firstMinute.getMinuteAsDate()))
				start = firstMinute.getMinuteAsDate();
			if ((end == null) || end.after(now))
				end = now;
			rebuildSummaries(_accountId, start, end);
			if (rate.isRecursAnnually()) {
				while (end.before(now)) {
					start = DateUtils.addYears(start, 1, tz);
					end = DateUtils.addYears(end, 1, tz);
					rebuildSummaries(_accountId, start, end);
				}
			}
		}
	}

	@Override
	public void rebuildSummaries(int _accountId) {
		HubPowerMinute firstMinute = proxy.queryOne(HubPowerMinute.class, new DaoQuery("account_id", _accountId), DaoSort.sort("minute"));
		if (firstMinute == null)
			return;
		rebuildSummaries(_accountId, firstMinute.getMinuteAsDate(), new Date());
	}

	@Override
	public void rebuildSummaries(int _accountId, Date _start, Date _end) {
		BreakerConfig config = getConfig(_accountId);
		TimeZone tz = getTimeZoneForAccount(_accountId);
		Date start = DateUtils.getMidnightBefore(_start, tz);
		Date monthStart = DateUtils.getStartOfMonth(_start, tz);
		BreakerGroup root = CollectionUtils.getFirst(config.getBreakerGroups());
		if (root == null)
			return;
		proxy.delete(BreakerGroupSummary.class, new DaoQuery("_id", BreakerGroupEnergy.toId(_accountId, root.getId(), EnergyBlockViewMode.MONTH, monthStart)));
		while (start.before(_end)) {
			Date dayEnd = DateUtils.getMidnightAfter(start, tz);
			DebugTimer timer = new DebugTimer("Time to rebuild one day");
			DebugTimer t1 = new DebugTimer("Loading hub power for day, account: " + _accountId + " day: " + DateUtils.format("MM/dd/yyyy", tz, start));
			List<HubPowerMinute> minutes = proxy.query(HubPowerMinute.class, new DaoQuery("account_id", _accountId).andBetweenInclusiveExclusive("minute", (int) (start.getTime() / 60000), (int) (dayEnd.getTime() / 60000)));
			t1.stop();
			monthStart = DateUtils.getStartOfMonth(start, tz);
			BreakerGroupSummary month = null;
			if (monthStart.equals(start))
				proxy.delete(BreakerGroupSummary.class, new DaoQuery("_id", BreakerGroupEnergy.toId(_accountId, root.getId(), EnergyBlockViewMode.MONTH, monthStart)));
			else
				month = proxy.queryOne(BreakerGroupSummary.class, new DaoQuery("_id", BreakerGroupEnergy.toId(_accountId, root.getId(), EnergyBlockViewMode.MONTH, monthStart)));
			BreakerGroupEnergy energy = new BreakerGroupEnergy(root, minutes, EnergyBlockViewMode.DAY, start, month, config.getBillingRates(), tz);
			timer.stop();
			putBreakerGroupEnergy(energy);
			updateSummaries(root, CollectionUtils.asHashSet(start), tz);
			start = DateUtils.addDays(start, 1, tz);
		}
	}

	@Override
	public void putBreakerGroupEnergy(BreakerGroupEnergy _energy) {
		proxy.save(_energy);
		proxy.save(new BreakerGroupSummary(_energy));
	}

	@Override
	public BreakerConfig getConfig(int _accountId) {
		return proxy.queryOne(BreakerConfig.class, new DaoQuery("_id", String.valueOf(_accountId)));
	}

	@Override
	public BreakerConfig getMergedConfig(AuthCode _authCode) {
		if (_authCode == null)
			return null;
		List<BreakerConfig> configs = CollectionUtils.transform(_authCode.getAllAccountIds(), this::getConfig, true);
		BreakerConfig config = new BreakerConfig();
		config.setAccountId(_authCode.getAccountId());
		config.setBreakerHubs(CollectionUtils.aggregate(configs, BreakerConfig::getBreakerHubs));
		config.setBreakerGroups(CollectionUtils.aggregate(configs, BreakerConfig::getBreakerGroups));
		config.setPanels(CollectionUtils.aggregate(configs, BreakerConfig::getPanels));
		config.setMeters(CollectionUtils.aggregate(configs, BreakerConfig::getMeters));
		config.setBillingRates(CollectionUtils.aggregate(configs, BreakerConfig::getBillingRates));
		return config;
	}

	@Override
	public void putConfig(BreakerConfig _config) {
		DaoQuery configQuery = new DaoQuery("_id", String.valueOf(_config.getAccountId()));
		BreakerConfig oldConfig = proxy.queryOne(BreakerConfig.class, configQuery);
		if (oldConfig != null) {
			_config.setVersion(oldConfig.getVersion() + 1);
			if (NullUtils.isNotIdentical(_config, oldConfig)) {
				DaoEntity oldEntity = DaoSerializer.toDaoEntity(oldConfig);
				oldEntity.put("_id", String.format("%d-%d", oldConfig.getAccountId(), oldConfig.getVersion()));
				oldEntity.put("account_id", oldConfig.getAccountId());
				oldEntity.put("archive_date", DaoSerializer.toLong(new Date()));
				proxy.saveEntity("config_archive", oldEntity);
				executor.submit(() -> {
					List<BillingRate> changedRates = new ArrayList<>(_config.getBillingRates());
					changedRates.removeAll(CollectionUtils.makeNotNull(oldConfig.getBillingRates()));
					if (!changedRates.isEmpty())
						rebuildSummaries(_config.getAccountId(), changedRates);
				});
			}
		}
		proxy.save(_config);
	}

	@Override
	public String authenticateAccount(String _username, String _password) {
		Account acct = proxy.queryOne(Account.class, new DaoQuery("username", _username));
		if ((acct == null) || !BCrypt.checkpw(_password, acct.getPassword()))
			return null;
		return toAuthCode(acct.getId(), acct.getAuxiliaryAccountIds());
	}

	@Override
	public Account authCodeToAccount(String _authCode) {
		AuthCode code = decryptAuthCode(_authCode);
		if (code == null)
			return null;
		return proxy.queryOne(Account.class, new DaoQuery("_id", code.getAccountId()));
	}

	@Override
	public AuthCode decryptAuthCode(String _authCode) {
		return DaoSerializer.fromZipBson(aes.decryptFromBase64(_authCode), AuthCode.class);
	}

	@Override
	public String getAuthCodeForEmail(String _email, TimeZone _tz) {
		_email = _email.toLowerCase().trim();
		Account account = getAccountByUsername(_email);
		if (account == null) {
			account = new Account();
			account.setUsername(_email);
			account.setTimezone(_tz.getID());
			putAccount(account);
		}
		return toAuthCode(account.getId(), account.getAuxiliaryAccountIds());
	}

	public String toAuthCode(int _acctId, List<Integer> _auxAcctIds) {
		if (_acctId < 1)
			return null;
		return aes.encryptToBase64(DaoSerializer.toZipBson(new AuthCode(_acctId, _auxAcctIds)));
	}

	@Override
	public Account putAccount(Account _account) {
		if (_account == null)
			return null;
		_account.setUsername(NullUtils.makeNotNull(_account.getUsername()).toLowerCase().trim());
		Account account = getAccountByUsername(_account.getUsername());
		if (account != null) {
			_account.setId(account.getId());
			if (NullUtils.isEmpty(_account.getPassword()))
				_account.setPassword(account.getPassword());
			else
				_account.setPassword(BCrypt.hashpw(_account.getPassword(), BCrypt.gensalt(BCRYPT_ROUNDS)));
		}
		else if (NullUtils.isNotEmpty(_account.getPassword())) {
			_account.setPassword(BCrypt.hashpw(_account.getPassword(), BCrypt.gensalt(BCRYPT_ROUNDS)));
		}
		if (_account.getId() == 0)
			_account.setId(proxy.updateOne(Sequence.class, null, new DaoEntity("$inc", new DaoEntity("sequence", 1))).getSequence());
		proxy.save(_account);
		return clearPassword(_account);
	}

	@Override
	public Account getAccount(int _accountId) {
		return clearPassword(proxy.queryOne(Account.class, new DaoQuery("_id", String.valueOf(_accountId))));
	}

	@Override
	public Account getAccountByUsername(String _username) {
		return clearPassword(proxy.queryOne(Account.class, new DaoQuery("username", NullUtils.makeNotNull(_username).toLowerCase().trim())));
	}

	@Override
	public TimeZone getTimeZoneForAccount(int _accountId) {
		String timezone = proxy.queryForOneField(Account.class, new DaoQuery("_id", String.valueOf(_accountId)), "timezone");
		TimeZone tz = null;
		try {
			if (NullUtils.isNotEmpty(timezone))
				tz = TimeZone.getTimeZone(timezone);
		}
		catch (Exception _e) {
			logger.error("TimeZone not configured correctly for account {}", _accountId);
		}
		return tz == null ? TimeZone.getTimeZone("America/Chicago") : tz;
	}

	@Override
	public String getTimeZoneForAccount(String _authCode) {
		AuthCode code = decryptAuthCode(_authCode);
		if (code == null)
			return null;
		return getTimeZoneForAccount(code.getAccountId()).getID();
	}

	private Account clearPassword(Account _account) {
		if (_account == null)
			return null;
		_account.setPassword(null);
		return _account;
	}

	@Override
	public String addPasswordResetKey(String _email) {
		String key = aes.encryptToUrlSafeBase64(_email);
		proxy.saveEntity("password_reset", new DaoEntity("_id", key));
		return key;
	}

	@Override
	public String getEmailForResetKey(String _key) {
		DaoEntity entity = proxy.queryForEntity("password_reset", new DaoQuery("_id", _key));
		if (entity == null)
			return null;
		return aes.decryptFromBase64ToString(_key);
	}

	@Override
	public boolean resetPassword(String _key, String _password) {
		DaoEntity entity = proxy.queryForEntity("password_reset", new DaoQuery("_id", _key));
		if (entity == null)
			return false;
		Account acct = getAccountByUsername(aes.decryptFromBase64ToString(_key));
		acct.setPassword(_password);
		putAccount(acct);
		proxy.delete("password_reset", new DaoQuery("_id", _key));
		return true;
	}

	@Override
	public MongoProxy getProxy() {
		return proxy;
	}
}
