package com.lanternsoftware.powermonitor.datamodel;


import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.mutable.MutableDouble;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

@DBSerializable(autogen = false)
public class EnergySummary {
	private int accountId;
	private String groupId;
	private String groupName;
	private EnergyViewMode viewMode;
	private Date start;
	private List<EnergySummary> subGroups;
	private boolean main;
	private float[] voltage;
	private float[] energy;
	private float[] gridEnergy;
	private double peakToGrid;
	private double peakFromGrid;
	private double peakConsumption;
	private double peakProduction;
	private TimeZone timezone;

	public EnergySummary() {
	}

	public EnergySummary(BreakerGroup _group, List<HubPowerMinute> _power, EnergyViewMode _viewMode, Date _start, TimeZone _timezone) {
		groupId = _group.getId();
		groupName = _group.getName();
		main = _group.isMain();
		viewMode = _viewMode;
		start = _start;
		accountId = _group.getAccountId();
		timezone = _timezone;
		subGroups = CollectionUtils.transform(_group.getSubGroups(), _g -> new EnergySummary(_g, null, _viewMode, _start, timezone));
		if (_power != null)
			addEnergy(_group, _power);
	}

	public void addEnergy(BreakerGroup _group, List<HubPowerMinute> _hubPower) {
		Map<Integer, Breaker> breakers = CollectionUtils.transformToMap(_group.getAllBreakers(), Breaker::getIntKey);
		Map<Integer, BreakerGroup> breakerKeyToGroup = new HashMap<>();
		for (BreakerGroup group : _group.getAllBreakerGroups()) {
			for (Breaker b : CollectionUtils.makeNotNull(group.getBreakers())) {
				breakerKeyToGroup.put(b.getIntKey(), group);
			}
		}
		addEnergy(breakers, breakerKeyToGroup, _hubPower);
	}

	private void addEnergy(Map<Integer, Breaker> _breakers, Map<Integer, BreakerGroup> _breakerKeyToGroup, List<HubPowerMinute> _hubPower) {
		if (CollectionUtils.isEmpty(_hubPower) || CollectionUtils.anyQualify(_hubPower, _p -> _p.getAccountId() != accountId))
			return;
		_hubPower.sort(Comparator.comparing(HubPowerMinute::getMinute));
		for (Date minute : CollectionUtils.transformToSet(_hubPower, HubPowerMinute::getMinuteAsDate)) {
			resetEnergy(minute);
		}
		Set<Integer> meterMainsTracked = CollectionUtils.transformToSet(CollectionUtils.filter(_breakers.values(), Breaker::isMain), Breaker::getMeter);
		int idx;
		Map<Integer, Map<Integer, MeterMinute>> minutes = new HashMap<>();
		for (HubPowerMinute hubPower : _hubPower) {
			Date minute = hubPower.getMinuteAsDate();
			for (BreakerPowerMinute breaker : CollectionUtils.makeNotNull(hubPower.getBreakers())) {
				int key = breaker.breakerIntKey();
				Breaker b = _breakers.get(key);
				if (b == null)
					continue;
				BreakerGroup group = _breakerKeyToGroup.get(key);
				if (group == null)
					continue;
				MeterMinute meter = minutes.computeIfAbsent(hubPower.getMinute(), _p -> new HashMap<>()).computeIfAbsent(b.getMeter(), _m -> new MeterMinute(b.getMeter(), minute));
				idx = 0;

				for (Float power : CollectionUtils.makeNotNull(breaker.getReadings())) {
					if (idx >= 60)
						break;
					if (!meterMainsTracked.contains(b.getMeter()) || b.isMain()) {
						if (power > 0)
							meter.usage[idx] += power;
						else
							meter.solar[idx] -= power;
					}
					addEnergy(group.getId(), minute, power);
					idx++;
				}
				setVoltage(group.getId(), minute, hubPower.getVoltage());
			}
		}
		double curConsumption;
		double curProduction;
		double curToGrid;
		double curFromGrid;
		for (Map<Integer, MeterMinute> meters : minutes.values()) {
			for (int i = 0; i < 60; i++) {
				curConsumption = 0;
				curProduction = 0;
				curToGrid = 0;
				curFromGrid = 0;
				for (MeterMinute minute : meters.values()) {
					curConsumption += minute.usage[i];
					curProduction += minute.solar[i];
					minute.flow[i] = minute.usage[i] - minute.solar[i];
					if (minute.flow[i] > 0)
						curFromGrid += minute.flow[i];
					else
						curToGrid -= minute.flow[i];
				}
				if (curConsumption > peakConsumption)
					peakConsumption = curConsumption;
				if (curProduction > peakProduction)
					peakProduction = curProduction;
				if (curToGrid > peakToGrid)
					peakToGrid = curToGrid;
				if (curFromGrid > peakFromGrid)
					peakFromGrid = curFromGrid;
			}
		}
		for (HubPowerMinute hubPower : _hubPower) {
			Date minute = hubPower.getMinuteAsDate();
			for (BreakerPowerMinute breaker : CollectionUtils.makeNotNull(hubPower.getBreakers())) {
				int key = breaker.breakerIntKey();
				Breaker b = _breakers.get(key);
				if (b == null)
					continue;
				BreakerGroup group = _breakerKeyToGroup.get(key);
				if (group == null)
					continue;
				MeterMinute meter = minutes.get(hubPower.getMinute()).get(b.getMeter());
				idx = 0;
				double flow = 0.0;
				for (Float power : CollectionUtils.makeNotNull(breaker.getReadings())) {
					if (power < 0 && (meter.flow[idx] < 0.0))
						flow -= meter.flow[idx] * (power / meter.solar[idx]);
					else if (power > 0 && (meter.flow[idx] > 0.0))
						flow += meter.flow[idx] * (power / meter.usage[idx]);
					idx++;
				}
				addFlow(group.getId(), minute, flow);
			}
		}
	}

	public void resetEnergy(Date _readTime) {
		if (energy != null) {
			int idx = viewMode.blockIndex(start, _readTime, timezone);
			if (idx < energy.length)
				energy[idx] = 0f;
		}
		for (EnergySummary subGroup : CollectionUtils.makeNotNull(subGroups)) {
			subGroup.resetEnergy(_readTime);
		}
	}

	public static EnergySummary summary(BreakerGroup _group, Map<String, List<EnergyTotal>> _energies, EnergyViewMode _viewMode, Date _start, TimeZone _tz) {
		EnergySummary energy = new EnergySummary();
		energy.setGroupId(_group.getId());
		energy.setGroupName(_group.getName());
		energy.setAccountId(_group.getAccountId());
		energy.setViewMode(_viewMode);
		energy.setStart(_start);
		energy.setTimeZone(_tz);
		energy.setSubGroups(CollectionUtils.transform(_group.getSubGroups(), _g -> EnergySummary.summary(_g, _energies, _viewMode, _start, _tz)));
		for (EnergyTotal curEnergy : CollectionUtils.makeNotNull(_energies.get(_group.getId()))) {
			energy.setVoltage(curEnergy.getStart(), curEnergy.getVoltage());
			energy.addEnergy(curEnergy.getStart(), curEnergy.getJoules());
			energy.addFlow(curEnergy.getStart(), curEnergy.getFlow());
			if (curEnergy.getPeakFromGrid() > energy.getPeakFromGrid())
				energy.setPeakFromGrid(curEnergy.getPeakFromGrid());
			if (curEnergy.getPeakToGrid() > energy.getPeakToGrid())
				energy.setPeakToGrid(curEnergy.getPeakToGrid());
			if (curEnergy.getPeakConsumption() > energy.getPeakConsumption())
				energy.setPeakConsumption(curEnergy.getPeakConsumption());
			if (curEnergy.getPeakProduction() > energy.getPeakProduction())
				energy.setPeakProduction(curEnergy.getPeakProduction());
		}
		return energy;
	}

	private boolean addEnergy(String _groupId, Date _readTime, double _joules) {
		if (NullUtils.isEqual(groupId, _groupId)) {
			addEnergy(_readTime, _joules);
			return true;
		} else {
			for (EnergySummary subGroup : CollectionUtils.makeNotNull(subGroups)) {
				if (subGroup.addEnergy(_groupId, _readTime, _joules))
					return true;
			}
		}
		return false;
	}

	private void addEnergy(Date _readTime, double _joules) {
		if (energy == null)
			energy = new float[blockCount()];
		int idx = viewMode.blockIndex(start, _readTime, timezone);
		if (idx < energy.length)
			energy[idx] += _joules;
	}

	private boolean setVoltage(String _groupId, Date _readTime, float _voltage) {
		if (NullUtils.isEqual(groupId, _groupId)) {
			setVoltage(_readTime, _voltage);
			return true;
		} else {
			for (EnergySummary subGroup : CollectionUtils.makeNotNull(subGroups)) {
				if (subGroup.setVoltage(_groupId, _readTime, _voltage))
					return true;
			}
		}
		return false;
	}

	private void setVoltage(Date _readTime, float _voltage) {
		if (voltage == null)
			voltage = new float[blockCount()];
		int idx = viewMode.blockIndex(start, _readTime, timezone);
		if (idx < voltage.length)
			voltage[idx] = _voltage;
	}

	private boolean addFlow(String _groupId, Date _readTime, double _joules) {
		if (NullUtils.isEqual(groupId, _groupId)) {
			addFlow(_readTime, _joules);
			return true;
		} else {
			for (EnergySummary subGroup : CollectionUtils.makeNotNull(subGroups)) {
				if (subGroup.addFlow(_groupId, _readTime, _joules))
					return true;
			}
		}
		return false;
	}

	private void addFlow(Date _readTime, double _joules) {
		if (gridEnergy == null)
			gridEnergy = new float[blockCount()];
		int idx = viewMode.blockIndex(start, _readTime, timezone);
		if (idx < gridEnergy.length)
			gridEnergy[idx] += _joules;
	}

	private int blockCount() {
		return viewMode.blockCount(start, timezone);
	}

	public String getId() {
		return toId(accountId, groupId, viewMode, start);
	}

	public static String toId(int _accountId, String _groupId, EnergyViewMode _viewMode, Date _start) {
		return _accountId + "-" + _groupId + "-" + DaoSerializer.toEnumName(_viewMode) + "-" + _start.getTime();
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String _groupId) {
		groupId = _groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String _groupName) {
		groupName = _groupName;
	}

	public EnergySummary getSubGroup(String _groupId) {
		if (NullUtils.isEqual(groupId, _groupId))
			return this;
		for (EnergySummary summary : CollectionUtils.makeNotNull(subGroups)) {
			EnergySummary subGroup = summary.getSubGroup(_groupId);
			if (subGroup != null)
				return subGroup;
		}
		return null;
	}

	public List<EnergySummary> getSubGroups() {
		return subGroups;
	}

	public EnergyViewMode getViewMode() {
		return viewMode;
	}

	public void setViewMode(EnergyViewMode _viewMode) {
		viewMode = _viewMode;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date _start) {
		start = _start;
	}

	public void setSubGroups(List<EnergySummary> _subGroups) {
		subGroups = _subGroups;
	}

	public boolean isMain() {
		return main;
	}

	public void setMain(boolean _main) {
		main = _main;
	}

	public float[] getVoltage() {
		return voltage;
	}

	public void setVoltage(float[] _voltage) {
		voltage = _voltage;
	}

	public float[] getEnergy() {
		return energy;
	}

	public void setEnergy(float[] _energy) {
		energy = _energy;
	}

	public float[] getGridEnergy() {
		return gridEnergy;
	}

	public void setGridEnergy(float[] _gridEnergy) {
		gridEnergy = _gridEnergy;
	}

	public double getPeakToGrid() {
		return peakToGrid;
	}

	public void setPeakToGrid(double _peakToGrid) {
		peakToGrid = _peakToGrid;
	}

	public double getPeakFromGrid() {
		return peakFromGrid;
	}

	public void setPeakFromGrid(double _peakFromGrid) {
		peakFromGrid = _peakFromGrid;
	}

	public double getPeakConsumption() {
		return peakConsumption;
	}

	public void setPeakConsumption(double _peakConsumption) {
		peakConsumption = _peakConsumption;
	}

	public double getPeakProduction() {
		return peakProduction;
	}

	public void setPeakProduction(double _peakProduction) {
		peakProduction = _peakProduction;
	}

	public TimeZone getTimeZone() {
		return timezone;
	}

	public void setTimeZone(TimeZone _timezone) {
		timezone = _timezone;
	}

	public double wattHours() {
		return joules() / 3600;
	}

	public double wattHours(Set<String> _selectedBreakers) {
		return joules(_selectedBreakers) / 3600;
	}

	public double joules() {
		return joules(null);
	}

	public double joules(Set<String> _selectedBreakers) {
		return joules(_selectedBreakers, true, GridFlow.BOTH);
	}

	public double joules(Set<String> _selectedBreakers, boolean _includeSubgroups, GridFlow _mode) {
		double joules = 0.0;
		if (_includeSubgroups && !isMain()) {
			for (EnergySummary group : CollectionUtils.makeNotNull(subGroups)) {
				joules += group.joules(_selectedBreakers, true, _mode);
			}
		}
		if ((energy != null) && ((_selectedBreakers == null) || _selectedBreakers.contains(getGroupId()))) {
			for (float block : energy) {
				if ((_mode == GridFlow.BOTH) || ((_mode == GridFlow.FROM) && (block > 0f)) || ((_mode == GridFlow.TO) && (block < 0f)))
					joules += block;
			}
		}
		return joules;
	}

	public double flow() {
		return flow(null);
	}

	public double flow(Set<String> _selectedBreakers) {
		return flow(_selectedBreakers, true, GridFlow.BOTH);
	}

	public double flow(Set<String> _selectedBreakers, boolean _includeSubgroups, GridFlow _mode) {
		double flow = 0.0;
		if (_includeSubgroups) {
			for (EnergySummary group : CollectionUtils.makeNotNull(subGroups)) {
				flow += group.flow(_selectedBreakers, true, _mode);
			}
		}
		if ((gridEnergy != null) && ((_selectedBreakers == null) || _selectedBreakers.contains(getGroupId()))) {
			for (float block : gridEnergy) {
				if ((_mode == GridFlow.BOTH) || ((_mode == GridFlow.FROM) && (block > 0f)) || ((_mode == GridFlow.TO) && (block < 0f)))
					flow += block;
			}
		}
		return flow;
	}

	public List<EnergySummary> getAllGroups() {
		Map<String, EnergySummary> groups = new TreeMap<>();
		getAllGroups(groups);
		return new ArrayList<>(groups.values());
	}

	public void getAllGroups(Map<String, EnergySummary> _groups) {
		_groups.put(getGroupId(), this);
		for (EnergySummary group : CollectionUtils.makeNotNull(subGroups)) {
			group.getAllGroups(_groups);
		}
	}

	public float[] voltageBlocks() {
		return voltageBlocks(null);
	}

	public float[] voltageBlocks(Set<String> _selectedGroups) {
		float[] blocks = new float[blockCount()];
		int count = voltageBlocks(_selectedGroups, blocks);
		if (count > 0) {
			for (int i = 0; i < blocks.length; i++) {
				blocks[i] /= count;
			}
		}
		return blocks;
	}

	private int voltageBlocks(Set<String> _selectedGroups, float[] _voltageBlocks) {
		int count = 0;
		if ((voltage != null) && ((_selectedGroups == null) || _selectedGroups.contains(getGroupId()))) {
			for (int i = 0; i < voltage.length; i++) {
				_voltageBlocks[i] += voltage[i];
			}
			count = 1;
		}
		for (EnergySummary group : CollectionUtils.makeNotNull(subGroups)) {
			count += group.voltageBlocks(_selectedGroups, _voltageBlocks);
		}
		return count;
	}

	public float[] energyBlocks() {
		return energyBlocks(null);
	}

	public float[] energyBlocks(Set<String> _selectedGroups) {
		return energyBlocks(_selectedGroups, GridFlow.BOTH);
	}

	public float[] energyBlocks(Set<String> _selectedGroups, GridFlow _flow) {
		float[] blocks = new float[blockCount()];
		energyBlocks(_selectedGroups, blocks, _flow);
		return blocks;
	}

	private void energyBlocks(Set<String> _selectedGroups, float[] _energyBlocks, GridFlow _flow) {
		if ((energy != null) && ((_selectedGroups == null) || _selectedGroups.contains(getGroupId()))) {
			for (int i = 0; i < energy.length; i++) {
				if ((_flow == GridFlow.BOTH) || ((_flow == GridFlow.FROM) && energy[i] >= 0.0) || ((_flow == GridFlow.TO) && energy[i] <= 0.0))
					_energyBlocks[i] += energy[i];
			}
		}
		for (EnergySummary group : CollectionUtils.makeNotNull(subGroups)) {
			group.energyBlocks(_selectedGroups, _energyBlocks, _flow);
		}
	}

	public List<ChargeSummary> toChargeSummaries(BreakerConfig _config, List<EnergyTotal> _totals) {
		Map<String, Integer> breakerGroupMeters = _config.getRootGroup().mapToMeters();
		return CollectionUtils.transform(_config.getBillingPlans(), _p->{
			double monthKwh = CollectionUtils.sum(CollectionUtils.transform(CollectionUtils.filter(_totals, _t->_t.getStart().getTime() >= _p.getBillingCycleStart(start, timezone).getTime()), EnergyTotal::totalJoules))/3600000.0;
			return toChargeSummary(_p.getPlanId(), _p.getRates(), breakerGroupMeters, new MutableDouble(monthKwh));
		});
	}

	public ChargeSummary toChargeSummary(int _planId, List<BillingRate> _rates, Map<String, Integer> _breakerGroupMeters, MutableDouble _monthKwh) {
		return toChargeSummaryForRates(_planId, CollectionUtils.filter(_rates, _r->_r.isApplicableForDay(start, timezone)), _breakerGroupMeters, _monthKwh);
	}

	private ChargeSummary toChargeSummaryForRates(int _planId, List<BillingRate> _rates, Map<String, Integer> _breakerGroupMeters, MutableDouble _monthKwh) {
		ChargeSummary summary = new ChargeSummary(accountId, _planId, groupId, groupName, viewMode, start, timezone);
		if (gridEnergy != null) {
			double[] charges = new double[gridEnergy.length];
			for (int i = 0; i < gridEnergy.length; i++) {
				_monthKwh.add(gridEnergy[i]/3600000.0);
				for (BillingRate rate : _rates) {
					if (gridEnergy[i] > 0f) {
						if (rate.isApplicable(GridFlow.FROM, DaoSerializer.toInteger(_breakerGroupMeters.get(groupId)), _monthKwh.getValue(), i*60))
							charges[i] += rate.apply(((double) gridEnergy[i]) / 3600000.0);
					} else if (rate.isApplicable(GridFlow.TO, DaoSerializer.toInteger(_breakerGroupMeters.get(groupId)), _monthKwh.getValue(), i*60))
						charges[i] += rate.apply(((double) gridEnergy[i]) / 3600000.0);
				}
			}
			summary.setCharges(charges);
		}
		summary.setSubGroups(CollectionUtils.transform(subGroups, _s->_s.toChargeSummary(_planId, _rates, _breakerGroupMeters, _monthKwh)));
		summary.setTotalUsageJoules(joules(null, true, GridFlow.FROM));
		summary.setTotalSolarJoules(Math.abs(joules(null, true, GridFlow.TO)));
		summary.setFromGridJoules(flow(null, true, GridFlow.FROM));
		summary.setToGridJoules(Math.abs(flow(null, true, GridFlow.TO)));
		summary.setPeakConsumption(peakConsumption);
		summary.setPeakProduction(peakProduction);
		summary.setPeakFromGrid(peakFromGrid);
		summary.setPeakToGrid(peakToGrid);
		return summary;
	}

	private static class MeterMinute {
		private final int meter;
		private final Date minute;

		public MeterMinute(int _meter, Date _minute) {
			meter = _meter;
			minute = _minute;
		}

		public int getMeter() {
			return meter;
		}

		public Date getMinute() {
			return minute;
		}

		public double[] usage = new double[60];
		public double[] solar = new double[60];
		public double[] flow = new double[60];
	}
}
