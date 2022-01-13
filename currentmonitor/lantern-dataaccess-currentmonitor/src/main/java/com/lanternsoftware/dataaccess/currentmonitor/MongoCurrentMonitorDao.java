package com.lanternsoftware.dataaccess.currentmonitor;

import com.lanternsoftware.datamodel.currentmonitor.Account;
import com.lanternsoftware.datamodel.currentmonitor.BillingPlan;
import com.lanternsoftware.datamodel.currentmonitor.BillingRate;
import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.datamodel.currentmonitor.BreakerConfig;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroup;
import com.lanternsoftware.datamodel.currentmonitor.BreakerHub;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPower;
import com.lanternsoftware.datamodel.currentmonitor.ChargeSummary;
import com.lanternsoftware.datamodel.currentmonitor.ChargeTotal;
import com.lanternsoftware.datamodel.currentmonitor.EnergySummary;
import com.lanternsoftware.datamodel.currentmonitor.EnergyTotal;
import com.lanternsoftware.datamodel.currentmonitor.EnergyViewMode;
import com.lanternsoftware.datamodel.currentmonitor.HubCommand;
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
import com.lanternsoftware.util.mutable.MutableDouble;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
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
		proxy.ensureIndex(EnergySummary.class, DaoSort.sort("account_id").then("group_id").then("view_mode"));
		proxy.ensureIndex(EnergyTotal.class, DaoSort.sort("account_id").then("group_id").then("view_mode").then("start"));
		proxy.ensureIndex(ChargeSummary.class, DaoSort.sort("account_id").then("plan_id").then("group_id").then("view_mode"));
		proxy.ensureIndex(ChargeTotal.class, DaoSort.sort("account_id").then("plan_id").then("group_id").then("view_mode").then("start"));
		proxy.ensureIndex(DirtyMinute.class, DaoSort.sort("posted"));
		for (DirtyMinute minute : proxy.queryAll(DirtyMinute.class)) {
			updateEnergySummaries(minute);
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
		delayTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				executor.submit(() -> {
					if (proxy.queryOneAndDelete(DirtyMinute.class, new DaoQuery("_id", minute.getId())) != null)
						updateEnergySummaries(new DirtyMinute(_power.getAccountId(), _power.getMinute(), new Date()));
				});
			}
		}, 10000);
	}

	private void updateEnergySummaries(DirtyMinute _minute) {
		DebugTimer timer = new DebugTimer("Updating summaries", logger);
		List<HubPowerMinute> minutes = proxy.query(HubPowerMinute.class, new DaoQuery("account_id", _minute.getAccountId()).and("minute", _minute.getMinute()));
		TimeZone tz = getTimeZoneForAccount(_minute.getAccountId());
		BreakerConfig config = getConfig(_minute.getAccountId());
		BreakerGroup group = CollectionUtils.getFirst(config.getBreakerGroups());
		Date day = DateUtils.getMidnightBefore(_minute.getMinuteAsDate(), tz);
		DebugTimer t2 = new DebugTimer("Updating energy", logger);
		EnergySummary energy = getEnergySummary(_minute.getAccountId(), group.getId(), EnergyViewMode.DAY, day);
		if (energy == null)
			energy = new EnergySummary(group, minutes, EnergyViewMode.DAY, day, tz);
		else
			energy.addEnergy(group, minutes);
		putEnergySummary(energy);
		updateEnergySummaries(group, CollectionUtils.asHashSet(day), tz);
		t2.stop();
		DebugTimer t3 = new DebugTimer("Updating charges", logger);
		updateChargeSummary(config, energy, tz);
		updateChargeSummaries(config, CollectionUtils.asHashSet(energy.getStart()), tz);
		t3.stop();
		timer.stop();
	}

	private void putChargeSummary(ChargeSummary _summary) {
		putChargeSummaries(CollectionUtils.asArrayList(_summary));
	}

	private void putChargeSummaries(Collection<ChargeSummary> _summaries) {
		proxy.save(_summaries);
		proxy.save(CollectionUtils.transform(_summaries, ChargeTotal::new));
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
	public EnergySummary getEnergySummary(int _accountId, String _groupId, EnergyViewMode _viewMode, Date _start) {
		return proxy.queryOne(EnergySummary.class, new DaoQuery("_id", EnergySummary.toId(_accountId, _groupId, _viewMode, _start)));
	}

	@Override
	public byte[] getEnergySummaryBinary(int _accountId, String _groupId, EnergyViewMode _viewMode, Date _start) {
		return DaoSerializer.toZipBson(proxy.queryForEntity(EnergySummary.class, new DaoQuery("_id", EnergySummary.toId(_accountId, _groupId, _viewMode, _start))));
	}

	@Override
	public byte[] getChargeSummaryBinary(int _accountId, int _planId, String _groupId, EnergyViewMode _viewMode, Date _start) {
		return DaoSerializer.toZipBson(proxy.queryForEntity(ChargeSummary.class, new DaoQuery("_id", ChargeSummary.toId(_accountId, _planId, _groupId, _viewMode, _start))));
	}

	private void updateEnergySummaries(BreakerGroup _rootGroup, Set<Date> _daysToSummarize, TimeZone _tz) {
		Set<Date> monthsToSummarize = CollectionUtils.transformToSet(_daysToSummarize, _c -> DateUtils.getStartOfMonth(_c, _tz));
		Set<Date> yearsToSummarize = CollectionUtils.transformToSet(monthsToSummarize, _c -> DateUtils.getStartOfYear(_c, _tz));
		for (Date month : monthsToSummarize) {
			Calendar calDayStart = DateUtils.toCalendar(month, _tz);
			Calendar end = DateUtils.getEndOfMonthCal(month, _tz);
			List<String> groupEnergyIds = new ArrayList<>();
			while (calDayStart.before(end)) {
				groupEnergyIds.add(EnergySummary.toId(_rootGroup.getAccountId(), _rootGroup.getId(), EnergyViewMode.DAY, calDayStart.getTime()));
				calDayStart.add(Calendar.DAY_OF_YEAR, 1);
			}
			List<EnergyTotal> groupEnergies = CollectionUtils.aggregate(proxy.query(EnergyTotal.class, DaoQuery.in("_id", groupEnergyIds)), EnergyTotal::flatten);
			Map<String, List<EnergyTotal>> energies = CollectionUtils.transformToMultiMap(groupEnergies, EnergyTotal::getGroupId);
			EnergySummary summary = EnergySummary.summary(_rootGroup, energies, EnergyViewMode.MONTH, month, _tz);
			putEnergySummary(summary);
		}
		for (Date year : yearsToSummarize) {
			Calendar calMonthStart = DateUtils.toCalendar(year, _tz);
			Calendar end = DateUtils.getEndOfYearCal(year, _tz);
			List<String> summaryIds = new ArrayList<>();
			while (calMonthStart.before(end)) {
				summaryIds.add(EnergySummary.toId(_rootGroup.getAccountId(), _rootGroup.getId(), EnergyViewMode.MONTH, calMonthStart.getTime()));
				calMonthStart.add(Calendar.MONTH, 1);
			}
			List<EnergyTotal> groupEnergies = CollectionUtils.aggregate(proxy.query(EnergyTotal.class, DaoQuery.in("_id", summaryIds)), EnergyTotal::flatten);
			Map<String, List<EnergyTotal>> energies = CollectionUtils.transformToMultiMap(groupEnergies, EnergyTotal::getGroupId);
			EnergySummary summary = EnergySummary.summary(_rootGroup, energies, EnergyViewMode.YEAR, year, _tz);
			putEnergySummary(summary);
		}
		List<EnergyTotal> groupEnergies = CollectionUtils.aggregate(proxy.query(EnergyTotal.class, new DaoQuery("account_id", _rootGroup.getAccountId()).and("group_id", _rootGroup.getId()).and("view_mode", EnergyViewMode.YEAR.name())), EnergyTotal::flatten);
		Map<String, List<EnergyTotal>> energies = CollectionUtils.transformToMultiMap(groupEnergies, EnergyTotal::getGroupId);
		EnergySummary summary = EnergySummary.summary(_rootGroup, energies, EnergyViewMode.ALL, new Date(0), _tz);
		putEnergySummary(summary);
	}

	private void updateChargeSummary(BreakerConfig _config, EnergySummary _energySummary, TimeZone _tz) {
		Date lookback = null;
		for (BillingPlan p : CollectionUtils.makeNotNull(_config.getBillingPlans())) {
			Date cycleStart = p.getBillingCycleStart(_energySummary.getStart(), _tz);
			if (cycleStart.after(_energySummary.getStart()))
				cycleStart = DateUtils.addMonths(cycleStart, -1, _tz);
			if ((lookback == null) || cycleStart.before(lookback))
				lookback = cycleStart;
		}
		if (lookback != null) {
			List<String> groupEnergyIds = new ArrayList<>();
			while (lookback.before(_energySummary.getStart())) {
				groupEnergyIds.add(EnergySummary.toId(_config.getAccountId(), _energySummary.getGroupId(), EnergyViewMode.DAY, lookback));
				lookback = DateUtils.addDays(lookback, 1, _tz);
			}
			List<EnergyTotal> totals = proxy.query(EnergyTotal.class, DaoQuery.in("_id", groupEnergyIds));
			putChargeSummaries(_energySummary.toChargeSummaries(_config, totals));
		}
	}

	private void updateChargeSummaries(BreakerConfig _config, Set<Date> _daysToSummarize, TimeZone _tz) {
		if (CollectionUtils.isEmpty(_config.getBillingPlans()))
			return;
		Set<Date> yearsToSummarize = CollectionUtils.transformToSet(_daysToSummarize, _c -> DateUtils.getStartOfYear(_c, _tz));
		BreakerGroup rootGroup = _config.getRootGroup();
		for (BillingPlan plan : _config.getBillingPlans()) {
			List<ChargeSummary> summaries = new ArrayList<>();
			Set<Date> monthsToSummarize = CollectionUtils.transformToSet(_daysToSummarize, _c -> plan.getBillingCycleStart(_c, _tz));
			for (Date month : monthsToSummarize) {
				Calendar monthDayStart = DateUtils.toCalendar(month, _tz);
				Calendar monthEnd = DateUtils.toCalendar(plan.getBillingCycleEnd(month, _tz), _tz);
				Set<String> monthSummaryIds = new HashSet<>();
				while (monthDayStart.before(monthEnd)) {
					monthSummaryIds.add(ChargeSummary.toId(rootGroup.getAccountId(), plan.getPlanId(), rootGroup.getId(), EnergyViewMode.DAY, monthDayStart.getTime()));
					monthDayStart.add(Calendar.DAY_OF_YEAR, 1);
				}
				List<ChargeTotal> monthTotals = CollectionUtils.aggregate(proxy.query(ChargeTotal.class, DaoQuery.in("_id", monthSummaryIds)), ChargeTotal::flatten);
				Map<String, List<ChargeTotal>> monthCharges = CollectionUtils.transformToMultiMap(monthTotals, ChargeTotal::getGroupId);
				summaries.add(new ChargeSummary(rootGroup, plan, monthCharges, EnergyViewMode.MONTH, month, _tz));
			}
			putChargeSummaries(summaries);
		}
		for (BillingPlan plan : _config.getBillingPlans()) {
			List<ChargeSummary> summaries = new ArrayList<>();
			for (Date year : yearsToSummarize) {
				Date yearStart = DateUtils.getStartOfYear(year, _tz);
				Date yearEnd = DateUtils.addYears(yearStart, 1, _tz);
				Date yearMonthStart = yearStart;
				Set<String> monthSummaryIds = new HashSet<>();
				Date loopEnd = DateUtils.addDays(yearEnd, 1, _tz);
				while (yearMonthStart.before(loopEnd)) {
					Date billingStart = plan.getBillingCycleStart(yearMonthStart, _tz);
					if (DateUtils.isBetween(billingStart, yearStart, yearEnd))
						monthSummaryIds.add(ChargeSummary.toId(rootGroup.getAccountId(), plan.getPlanId(), rootGroup.getId(), EnergyViewMode.MONTH, billingStart));
					yearMonthStart = DateUtils.addMonths(yearMonthStart, 1, _tz);
				}
				List<ChargeTotal> flatTotals = CollectionUtils.aggregate(proxy.query(ChargeTotal.class, DaoQuery.in("_id", monthSummaryIds)), ChargeTotal::flatten);
				Map<String, List<ChargeTotal>> yearCharges = CollectionUtils.transformToMultiMap(flatTotals, ChargeTotal::getGroupId);
				summaries.add(new ChargeSummary(rootGroup, plan, yearCharges, EnergyViewMode.YEAR, yearStart, _tz));
			}
			putChargeSummaries(summaries);
		}
		for (BillingPlan plan : _config.getBillingPlans()) {
			List<ChargeTotal> yearTotals = CollectionUtils.aggregate(proxy.query(ChargeTotal.class, new DaoQuery("account_id", rootGroup.getAccountId()).and("plan_id", plan.getPlanId()).and("group_id", rootGroup.getId()).and("view_mode", EnergyViewMode.YEAR.name())), ChargeTotal::flatten);
			Map<String, List<ChargeTotal>> charges = CollectionUtils.transformToMultiMap(yearTotals, ChargeTotal::getGroupId);
			ChargeSummary summary = new ChargeSummary(rootGroup, plan, charges, EnergyViewMode.ALL, new Date(0), _tz);
			putChargeSummary(summary);
		}
	}

	private void rebuildSummaries(int _accountId, Collection<BillingRate> _rates) {
		logger.info("Rebuilding summaries due to a change in {} rates", CollectionUtils.size(_rates));
		HubPowerMinute firstMinute = proxy.queryOne(HubPowerMinute.class, new DaoQuery("account_id", _accountId), DaoSort.sort("minute"));
		if (firstMinute == null)
			return;
		TimeZone tz = getTimeZoneForAccount(_accountId);
		Map<String, BillingRate> rates = CollectionUtils.transformToMap(_rates, _r -> String.format("%d%d", DaoSerializer.toLong(_r.getBeginEffective()), DaoSerializer.toLong(_r.getEndEffective())));
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
	public void rebuildSummariesAsync(int _accountId) {
		executor.submit(() -> rebuildSummaries(_accountId));
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
		if (config == null)
			return;
		TimeZone tz = getTimeZoneForAccount(_accountId);
		Date start = DateUtils.getMidnightBefore(_start, tz);
		BreakerGroup root = CollectionUtils.getFirst(config.getBreakerGroups());
		if (root == null)
			return;
		Set<Date> dates = new HashSet<>();
		while (start.before(_end)) {
			Date dayEnd = DateUtils.getMidnightAfter(start, tz);
            DebugTimer timer = new DebugTimer("Time to rebuild one day", logger);
            DebugTimer t1 = new DebugTimer("Loading hub power for day, account: " + _accountId + " day: " + DateUtils.format("MM/dd/yyyy", tz, start), logger);
            List<HubPowerMinute> minutes = proxy.query(HubPowerMinute.class, new DaoQuery("account_id", _accountId).andBetweenInclusiveExclusive("minute", (int) (start.getTime() / 60000), (int) (dayEnd.getTime() / 60000)));
            t1.stop();
            if (!minutes.isEmpty()) {
                DebugTimer t2 = new DebugTimer("In memory rebuild", logger);
                EnergySummary energy = new EnergySummary(root, minutes, EnergyViewMode.DAY, start, tz);
                t2.stop();
                timer.stop();
                putEnergySummary(energy);
                DebugTimer t3 = new DebugTimer("Updating charges", logger);
                updateChargeSummary(config, energy, tz);
                t3.stop();
            }
			dates.add(start);
			start = DateUtils.addDays(start, 1, tz);
		}
		DebugTimer t4 = new DebugTimer("Updating month/year/lifetime energy summaries", logger);
		updateEnergySummaries(root, dates, tz);
		t4.stop();
		DebugTimer t5 = new DebugTimer("Updating month/year/lifetime charge summaries", logger);
		updateChargeSummaries(config, dates, tz);
		t5.stop();
	}

	public void rebuildChargeSummaries(BreakerConfig _config, BillingPlan _plan) {
		TimeZone tz = getTimeZoneForAccount(_config.getAccountId());
		HubPowerMinute firstMinute = proxy.queryOne(HubPowerMinute.class, new DaoQuery("account_id", _config.getAccountId()), DaoSort.sort("minute"));
		if (firstMinute == null)
			return;
		Date start = DateUtils.getMidnightBefore(firstMinute.getMinuteAsDate(), tz);
		Date end = DateUtils.getMidnightAfter(new Date(), tz);
		BreakerGroup root = CollectionUtils.getFirst(_config.getBreakerGroups());
		if (root == null)
			return;
		Set<Date> dates = new HashSet<>();
		Date curDate = start;
		while (curDate.before(end)) {
			dates.add(curDate);
			curDate = DateUtils.addDays(curDate, 1, tz);
		}
		List<String> summaryIds = CollectionUtils.transform(dates, _dt->EnergySummary.toId(_config.getAccountId(), root.getId(), EnergyViewMode.DAY, _dt));
		DebugTimer t1 = new DebugTimer("Load Daily Energy Totals", logger);
		Map<Date, EnergyTotal> totals = CollectionUtils.transformToMap(proxy.query(EnergyTotal.class, DaoQuery.in("_id", summaryIds)), EnergyTotal::getStart);
		t1.stop();
		Map<String, Integer> breakerGroupMeters = _config.getRootGroup().mapToMeters();
		List<ChargeSummary> chargeSummaries = new ArrayList<>();
		DebugTimer t2 = new DebugTimer("Load Energy Summaries", logger);
		List<EnergySummary> energySummaries = proxy.query(EnergySummary.class, DaoQuery.in("_id", summaryIds));
		t2.stop();
		DebugTimer t3 = new DebugTimer("Rebuild Charges Summaries", logger);
		for (EnergySummary energy : energySummaries) {
			Date cycleStart = _plan.getBillingCycleStart(energy.getStart(), tz);
			double monthKwh = 0.0;
			while (cycleStart.before(energy.getStart())) {
				EnergyTotal total = totals.get(cycleStart);
				if (total != null)
					monthKwh += total.totalJoules();
				cycleStart = DateUtils.addDays(cycleStart, 1, tz);
			}
			monthKwh /= 3600000.0;
			chargeSummaries.add(energy.toChargeSummary(_plan.getPlanId(), _plan.getRates(), breakerGroupMeters, new MutableDouble(monthKwh)));
		}
		t3.stop();
		DebugTimer t4 = new DebugTimer("Persist Charge Summaries", logger);
		putChargeSummaries(chargeSummaries);
		t4.stop();
		DebugTimer t5 = new DebugTimer("Updating month/year/lifetime charge summaries", logger);
		updateChargeSummaries(_config, dates, tz);
		t5.stop();
	}

	@Override
	public void putEnergySummary(EnergySummary _energy) {
		proxy.save(_energy);
		proxy.save(new EnergyTotal(_energy));
	}

	@Override
	public BreakerConfig getConfig(int _accountId) {
		return proxy.queryOne(BreakerConfig.class, new DaoQuery("_id", String.valueOf(_accountId)));
	}

	@Override
	public BreakerConfig getMergedConfig(AuthCode _authCode) {
		if (_authCode == null)
			return null;
		if (CollectionUtils.size(_authCode.getAllAccountIds()) == 1) {
			BreakerConfig config = getConfig(_authCode.getAccountId());
			if (config == null) {
				config = new BreakerConfig();
				config.setAccountId(_authCode.getAccountId());
				config.setVersion(config.getVersion());
				return config;
			}
		}
		List<BreakerConfig> configs = CollectionUtils.transform(_authCode.getAllAccountIds(), this::getConfig, true);
		BreakerConfig config = new BreakerConfig();
		config.setAccountId(_authCode.getAccountId());
		config.setBreakerHubs(CollectionUtils.aggregate(configs, BreakerConfig::getBreakerHubs));
		config.setBreakerGroups(CollectionUtils.aggregate(configs, BreakerConfig::getBreakerGroups));
		config.setPanels(CollectionUtils.aggregate(configs, BreakerConfig::getPanels));
		config.setMeters(CollectionUtils.aggregate(configs, BreakerConfig::getMeters));
		config.setBillingPlans(CollectionUtils.aggregate(configs, BreakerConfig::getBillingPlans));
		config.setBillingRates(CollectionUtils.aggregate(configs, BreakerConfig::getBillingRates));
		config.setVersion(CollectionUtils.getLargest(CollectionUtils.transform(configs, BreakerConfig::getVersion)));
		return config;
	}

	@Override
	public void putConfig(BreakerConfig _config) {
		DaoQuery configQuery = new DaoQuery("_id", String.valueOf(_config.getAccountId()));
		BreakerConfig oldConfig = proxy.queryOne(BreakerConfig.class, configQuery);
		if (oldConfig != null) {
			logger.info("old version: {}, new version:  {}", oldConfig.getVersion(), _config.getVersion());
			if (oldConfig.getVersion() > _config.getVersion()) {
				for (BreakerHub hub : CollectionUtils.makeNotNull(_config.getBreakerHubs())) {
					BreakerHub oldHub = oldConfig.getHub(hub.getHub());
					if (oldHub != null) {
						logger.info("Prevent overwrite of voltage calibration");
						hub.setVoltageCalibrationFactor(oldHub.getRawVoltageCalibrationFactor());
					}
				}
			}
			_config.setVersion(oldConfig.getVersion() + 1);
			if (NullUtils.isNotIdentical(_config, oldConfig)) {
				DaoEntity oldEntity = DaoSerializer.toDaoEntity(oldConfig);
				oldEntity.put("_id", String.format("%d-%d", oldConfig.getAccountId(), oldConfig.getVersion()));
				oldEntity.put("account_id", oldConfig.getAccountId());
				oldEntity.put("archive_date", DaoSerializer.toLong(new Date()));
				proxy.saveEntity("config_archive", oldEntity);
				executor.submit(() -> {
					Map<Integer, BillingPlan> oldPlans = CollectionUtils.transformToMap(oldConfig.getBillingPlans(), BillingPlan::getPlanId);
					for (BillingPlan plan : CollectionUtils.makeNotNull(_config.getBillingPlans())) {
						BillingPlan oldPlan = oldPlans.get(plan.getPlanId());
						if ((oldPlan == null) || !oldPlan.isIdentical(plan))
							rebuildChargeSummaries(_config, plan);
					}
				});
			}
		}
		for (BreakerHub hub : CollectionUtils.makeNotNull(_config.getBreakerHubs())) {
			logger.info("voltage calibration hub {}: {}", hub.getHub(), hub.getVoltageCalibrationFactor());
		}
		proxy.save(_config);
	}

	@Override
	public String authenticateAccount(String _username, String _password) {
		if (NullUtils.isEmpty(_username) || NullUtils.isEmpty(_password))
			return null;
		Account acct = proxy.queryOne(Account.class, new DaoQuery("username", _username.toLowerCase().trim()));
		if ((acct == null) || !BCrypt.checkpw(_password, acct.getPassword()))
			return null;
		return toAuthCode(acct.getId(), acct.getAuxiliaryAccountIds());
	}

	@Override
	public Account authCodeToAccount(String _authCode) {
		AuthCode code = decryptAuthCode(_authCode);
		if (code == null)
			return null;
		return proxy.queryOne(Account.class, new DaoQuery("_id", String.valueOf(code.getAccountId())));
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
		} else if (NullUtils.isNotEmpty(_account.getPassword())) {
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
		} catch (Exception _e) {
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
	public void putHubCommand(HubCommand _command) {
		BreakerConfig config = getConfig(_command.getAccountId());
		if (config != null)
			proxy.save(_command.forAllHubs(config));
	}

	@Override
	public List<HubCommand> getAllHubCommands() {
		return proxy.queryAll(HubCommand.class);
	}

	@Override
	public void deleteHubCommand(String _id) {
		proxy.delete(HubCommand.class, new DaoQuery("_id", _id));
	}

	@Override
	public MongoProxy getProxy() {
		return proxy;
	}
}
