package com.lanternsoftware.dataaccess.currentmonitor;

import com.lanternsoftware.datamodel.currentmonitor.Account;
import com.lanternsoftware.datamodel.currentmonitor.AuthCode;
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
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.cryptography.AESTool;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.DaoSort;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.util.dao.mongo.MongoProxy;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
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
	private static final AESTool aes = new AESTool(ResourceLoader.loadFile(LanternFiles.OPS_PATH + "authKey.dat"));
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
		BreakerGroupEnergy summary = getBreakerGroupEnergy(_minute.getAccountId(), group.getId(), EnergyBlockViewMode.DAY, day);
		if (summary == null)
			summary = new BreakerGroupEnergy(group, minutes, EnergyBlockViewMode.DAY, day, tz);
		else
			summary.addEnergy(group, minutes);
		putBreakerGroupEnergy(summary);
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
				calMonthStart.add(Calendar.DAY_OF_YEAR, 1);
			}
			List<BreakerGroupSummary> groupEnergies = CollectionUtils.aggregate(proxy.query(BreakerGroupSummary.class, DaoQuery.in("_id", groupEnergyIds)), BreakerGroupSummary::getAllGroups);
			Map<String, List<BreakerGroupSummary>> energies = CollectionUtils.transformToMultiMap(groupEnergies, BreakerGroupSummary::getGroupId);
			BreakerGroupEnergy summary = BreakerGroupEnergy.summary(_rootGroup, energies, EnergyBlockViewMode.YEAR, year, _tz);
			putBreakerGroupEnergy(summary);
		}
		List<BreakerGroupSummary> groupEnergies = CollectionUtils.aggregate(proxy.query(BreakerGroupSummary.class, new DaoQuery("group_id", _rootGroup.getId()).and("view_mode", EnergyBlockViewMode.YEAR.name())), BreakerGroupSummary::getAllGroups);
		Map<String, List<BreakerGroupSummary>> energies = CollectionUtils.transformToMultiMap(groupEnergies, BreakerGroupSummary::getGroupId);
		BreakerGroupEnergy summary = BreakerGroupEnergy.summary(_rootGroup, energies, EnergyBlockViewMode.ALL, new Date(0), _tz);
		putBreakerGroupEnergy(summary);
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
		return config;
	}

	@Override
	public void putConfig(BreakerConfig _config) {
		DaoQuery configQuery = new DaoQuery("_id", String.valueOf(_config.getAccountId()));
		BreakerConfig oldConfig = proxy.queryOne(BreakerConfig.class, configQuery);
		if (oldConfig != null) {
			proxy.saveEntity("config_archive", DaoSerializer.toDaoEntity(oldConfig));
			_config.setVersion(oldConfig.getVersion() + 1);
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
	public MongoProxy getProxy() {
		return proxy;
	}
}
