package com.lanternsoftware.datamodel.currentmonitor;


import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

@DBSerializable(autogen = false)
public class BreakerGroupEnergy {
	private int accountId;
	private String groupId;
	private String groupName;
	private EnergyBlockViewMode viewMode;
	private Date start;
	private List<BreakerGroupEnergy> subGroups;
	private List<EnergyBlock> energyBlocks;
	private double toGrid;
	private double fromGrid;
	private TimeZone timezone;

	public BreakerGroupEnergy() {
	}

	public BreakerGroupEnergy(BreakerGroup _group, List<HubPowerMinute> _power, EnergyBlockViewMode _viewMode, Date _start, BreakerGroupSummary _month, List<BillingRate> _rates, TimeZone _timezone) {
		groupId = _group.getId();
		groupName = _group.getName();
		viewMode = _viewMode;
		start = _start;
		accountId = _group.getAccountId();
		timezone = _timezone;
		subGroups = CollectionUtils.transform(_group.getSubGroups(), _g -> new BreakerGroupEnergy(_g, null, _viewMode, _start, _month, _rates, timezone));
		addEnergy(_group, _power, _month, _rates);
	}

	public void addEnergy(BreakerGroup _group, List<HubPowerMinute> _hubPower, BreakerGroupSummary _month, List<BillingRate> _rates) {
		Map<String, Breaker> breakers = CollectionUtils.transformToMap(_group.getAllBreakers(), Breaker::getKey);
		Map<String, BreakerGroup> breakerKeyToGroup = new HashMap<>();
		for (BreakerGroup group : _group.getAllBreakerGroups()) {
			for (Breaker b : group.getAllBreakers()) {
				breakerKeyToGroup.put(b.getKey(), group);
			}
		}
		addEnergy(breakers, breakerKeyToGroup, _hubPower, _month, _rates);
	}

	public void addEnergy(Map<String, Breaker> _breakers, Map<String, BreakerGroup> _breakerKeyToGroup, List<HubPowerMinute> _hubPower, BreakerGroupSummary _month, List<BillingRate> _rates) {
		if (CollectionUtils.isEmpty(_hubPower) || CollectionUtils.anyQualify(_hubPower, _p->_p.getAccountId() != accountId))
			return;
		_hubPower.sort(Comparator.comparing(HubPowerMinute::getMinute));
		for (Date minute : CollectionUtils.transformToSet(_hubPower, HubPowerMinute::getMinuteAsDate)) {
			resetEnergy(minute);
		}
		int idx;
		Map<MeterMinute, MeterMinuteValues> meters = new HashMap<>();
		for (HubPowerMinute hubPower : _hubPower) {
			Date minute = hubPower.getMinuteAsDate();
			for (BreakerPowerMinute breaker : CollectionUtils.makeNotNull(hubPower.getBreakers())) {
				Breaker b = _breakers.get(breaker.breakerKey());
				if (b == null)
					continue;
				BreakerGroup group = _breakerKeyToGroup.get(breaker.breakerKey());
				if (group == null)
					continue;
				MeterMinuteValues meter = meters.computeIfAbsent(new MeterMinute(b.getMeter(), minute), _p->new MeterMinuteValues());
				idx = 0;
				EnergyBlock block = getBlock(group.getId(), minute);
				if (block != null) {
					for (Float power : CollectionUtils.makeNotNull(breaker.getReadings())) {
						if (idx >= 60)
							break;
						if (power > 0)
							meter.usage[idx] += power;
						else
							meter.solar[idx] -= power;
						block.addJoules(power);
						idx++;
					}
				}
			}
		}
		double monthFromGrid = _month == null ? 0.0 : _month.getFromGrid();
		double secondFromGrid;
		for (Map.Entry<MeterMinute, MeterMinuteValues> meter : meters.entrySet()) {
			double monthkWh = monthFromGrid/3600000;
			List<BillingRate> consumptionRates = CollectionUtils.filter(_rates, _r->_r.isApplicable(BillingMode.CONSUMPTION, meter.getKey().meter, monthkWh, meter.getKey().minute, timezone));
			List<BillingRate> productionRates = CollectionUtils.filter(_rates, _r->_r.isApplicable(BillingMode.PRODUCTION, meter.getKey().meter, monthkWh, meter.getKey().minute, timezone));
			for (int i = 0; i < 60; i++) {
				secondFromGrid = meter.getValue().usage[i] - meter.getValue().solar[i];
				monthFromGrid += secondFromGrid;
				if (secondFromGrid > 0) {
					fromGrid += secondFromGrid;
					for (BillingRate rate : consumptionRates) {
						meter.getValue().charges[i] += rate.apply(secondFromGrid/3600000);
					}
				}
				else {
					toGrid -= secondFromGrid;
					for (BillingRate rate : productionRates) {
						meter.getValue().charges[i] += rate.apply(secondFromGrid/3600000);
					}
				}
			}
		}
		for (HubPowerMinute hubPower : _hubPower) {
			Date minute = hubPower.getMinuteAsDate();
			for (BreakerPowerMinute breaker : CollectionUtils.makeNotNull(hubPower.getBreakers())) {
				Breaker b = _breakers.get(breaker.breakerKey());
				if (b == null)
					continue;
				BreakerGroup group = _breakerKeyToGroup.get(breaker.breakerKey());
				if (group == null)
					continue;
				MeterMinuteValues meter = meters.get(new MeterMinute(b.getMeter(), minute));
				idx = 0;
				double charge = 0.0;
				for (Float power : CollectionUtils.makeNotNull(breaker.getReadings())) {
					if (b.getPolarity() == BreakerPolarity.SOLAR) {
						if (meter.charges[idx] < 0.0)
							charge -= meter.charges[idx] * (power/meter.solar[idx]);
					}
					else if (meter.charges[idx] > 0.0)
						charge += meter.charges[idx] * (power/meter.usage[idx]);
					idx++;
				}
				addCharge(group.getId(), minute, charge);
			}
		}
	}

	public void resetEnergy(Date _readTime) {
		EnergyBlock block = getBlock(_readTime, false);
		if (block != null)
			block.setJoules(0);
		for (BreakerGroupEnergy subGroup : CollectionUtils.makeNotNull(subGroups)) {
			subGroup.resetEnergy(_readTime);
		}
	}

	private EnergyBlock getBlock(String _groupId, Date _readTime) {
		if (NullUtils.isEqual(groupId, _groupId))
			return getBlock(_readTime);
		else {
			for (BreakerGroupEnergy subGroup : CollectionUtils.makeNotNull(subGroups)) {
				EnergyBlock block = subGroup.getBlock(_groupId, _readTime);
				if (block != null)
					return block;
			}
			return null;
		}
	}

	private void addEnergy(String _groupId, Date _readTime, double _joules) {
		EnergyBlock block = getBlock(_groupId, _readTime);
		if (block != null)
			block.addJoules(_joules);
	}

	private void addCharge(String _groupId, Date _readTime, double _charge) {
		EnergyBlock block = getBlock(_groupId, _readTime);
		if (block != null)
			block.addCharge(_charge);
	}

	public static BreakerGroupEnergy summary(BreakerGroup _group, Map<String, List<BreakerGroupSummary>> _energies, EnergyBlockViewMode _viewMode, Date _start, TimeZone _tz) {
		BreakerGroupEnergy energy = new BreakerGroupEnergy();
		energy.setGroupId(_group.getId());
		energy.setGroupName(_group.getName());
		energy.setAccountId(_group.getAccountId());
		energy.setViewMode(_viewMode);
		energy.setStart(_start);
		energy.setTimeZone(_tz);
		energy.setSubGroups(CollectionUtils.transform(_group.getSubGroups(), _g -> BreakerGroupEnergy.summary(_g, _energies, _viewMode, _start, _tz)));
		for (BreakerGroupSummary curEnergy : CollectionUtils.makeNotNull(_energies.get(_group.getId()))) {
			EnergyBlock block = energy.getBlock(curEnergy.getStart());
			block.addJoules(curEnergy.getJoules());
			block.addCharge(curEnergy.getCharge());
			energy.setToGrid(energy.getToGrid()+curEnergy.getToGrid());
			energy.setFromGrid(energy.getFromGrid()+curEnergy.getFromGrid());
		}
		return energy;
	}

	private EnergyBlock getBlock(Date _readTime) {
		return getBlock(_readTime, true);
	}

	private EnergyBlock getBlock(Date _readTime, boolean _add) {
		int size = CollectionUtils.size(energyBlocks);
		int idx = viewMode.blockIndex(start, _readTime, timezone);
		if (_add && (idx >= size)) {
			if (energyBlocks == null)
				energyBlocks = new ArrayList<>(viewMode.initBlockCount());
			LinkedList<EnergyBlock> newBlocks = new LinkedList<>();
			Date end = viewMode.toBlockEnd(_readTime, timezone);
			while (idx >= size) {
				Date start = viewMode.decrementBlock(end, timezone);
				newBlocks.add(new EnergyBlock(start, end, 0));
				end = start;
				size++;
			}
			Iterator<EnergyBlock> iter = newBlocks.descendingIterator();
			while (iter.hasNext()) {
				energyBlocks.add(iter.next());
			}
		}
		return CollectionUtils.get(energyBlocks, idx);
	}

	public String getId() {
		return toId(accountId, groupId, viewMode, start);
	}

	public static String toId(int _accountId, String _groupId, EnergyBlockViewMode _viewMode, Date _start) {
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

	public BreakerGroupEnergy getSubGroup(String _groupId) {
		return CollectionUtils.filterOne(subGroups, _g->_groupId.equals(_g.getGroupId()));
	}

	public List<BreakerGroupEnergy> getSubGroups() {
		return subGroups;
	}

	public EnergyBlockViewMode getViewMode() {
		return viewMode;
	}

	public void setViewMode(EnergyBlockViewMode _viewMode) {
		viewMode = _viewMode;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date _start) {
		start = _start;
	}

	public void setSubGroups(List<BreakerGroupEnergy> _subGroups) {
		subGroups = _subGroups;
	}

	public List<EnergyBlock> getEnergyBlocks() {
		return energyBlocks;
	}

	public void setEnergyBlocks(List<EnergyBlock> _energyBlocks) {
		energyBlocks = _energyBlocks;
	}

	public double getToGrid() {
		return toGrid;
	}

	public void setToGrid(double _toGrid) {
		toGrid = _toGrid;
	}

	public double getFromGrid() {
		return fromGrid;
	}

	public void setFromGrid(double _fromGrid) {
		fromGrid = _fromGrid;
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
		return joules(_selectedBreakers, true);
	}

	public double joules(Set<String> _selectedBreakers, boolean _includeSubgroups) {
		double joules = 0.0;
		if (_includeSubgroups) {
			for (BreakerGroupEnergy group : CollectionUtils.makeNotNull(subGroups)) {
				joules += group.joules(_selectedBreakers);
			}
		}
		if ((energyBlocks != null) && ((_selectedBreakers == null) || _selectedBreakers.contains(getGroupId()))) {
			for (EnergyBlock energy : energyBlocks) {
				joules += energy.getJoules();
			}
		}
		return joules;
	}

	public double charge() {
		return charge(null);
	}

	public double charge(Set<String> _selectedBreakers) {
		return charge(_selectedBreakers, true);
	}

	public double charge(Set<String> _selectedBreakers, BillingMode _mode) {
		return charge(_selectedBreakers, true, _mode);
	}

	public double charge(Set<String> _selectedBreakers, boolean _includeSubgroups) {
		return charge(_selectedBreakers, _includeSubgroups, null);
	}

	public double charge(Set<String> _selectedBreakers, boolean _includeSubgroups, BillingMode _mode) {
		double charge = 0.0;
		if (_includeSubgroups) {
			for (BreakerGroupEnergy group : CollectionUtils.makeNotNull(subGroups)) {
				charge += group.charge(_selectedBreakers, true, _mode);
			}
		}
		if ((energyBlocks != null) && ((_selectedBreakers == null) || _selectedBreakers.contains(getGroupId()))) {
			for (EnergyBlock energy : energyBlocks) {
				if ((_mode == null) || ((_mode == BillingMode.PRODUCTION) && energy.getCharge() < 0.0) || (_mode == BillingMode.CONSUMPTION && energy.getCharge() > 0.0))
					charge += energy.getCharge();
			}
		}
		return charge;
	}

	public List<BreakerGroupEnergy> getAllGroups() {
		Map<String, BreakerGroupEnergy> groups = new TreeMap<>();
		getAllGroups(groups);
		return new ArrayList<>(groups.values());
	}

	public void getAllGroups(Map<String, BreakerGroupEnergy> _groups) {
		_groups.put(getGroupId(), this);
		for (BreakerGroupEnergy group : CollectionUtils.makeNotNull(subGroups)) {
			group.getAllGroups(_groups);
		}
	}

	public List<EnergyBlock> getAllEnergyBlocks() {
		return getAllEnergyBlocks(null);
	}

	public List<EnergyBlock> getAllEnergyBlocks(Set<String> _selectedGroups) {
		return getAllEnergyBlocks(_selectedGroups, EnergyBlockType.ANY);
	}

	public List<EnergyBlock> getAllEnergyBlocks(Set<String> _selectedGroups, EnergyBlockType _type) {
		Map<Long, EnergyBlock> blocks = new TreeMap<>();
		getAllEnergyBlocks(_selectedGroups, blocks, _type);
		return new ArrayList<>(blocks.values());
	}

	private void getAllEnergyBlocks(Set<String> _selectedGroups, Map<Long, EnergyBlock> _energyBlocks, EnergyBlockType _type) {
		if ((energyBlocks != null) && ((_selectedGroups == null) || _selectedGroups.contains(getGroupId()))) {
			for (EnergyBlock block : energyBlocks) {
				if ((_type == EnergyBlockType.ANY) || ((_type == EnergyBlockType.POSITIVE) && block.getJoules() >= 0.0) || ((_type == EnergyBlockType.NEGATIVE) && block.getJoules() <= 0.0)) {
					EnergyBlock b = _energyBlocks.get(block.getStart().getTime());
					if (b == null) {
						b = new EnergyBlock(block.getStart(), block.getEnd(), block.getJoules());
						_energyBlocks.put(block.getStart().getTime(), b);
					} else
						b.addJoules(block.getJoules());
					b.addCharge(block.getCharge());
				}
			}
		}
		for (BreakerGroupEnergy group : CollectionUtils.makeNotNull(subGroups)) {
			group.getAllEnergyBlocks(_selectedGroups, _energyBlocks, _type);
		}
	}

	private static class MeterMinute {
		private final int meter;
		private final Date minute;

		public MeterMinute(int _meter, Date _minute) {
			meter = _meter;
			minute = _minute;
		}

		@Override
		public boolean equals(Object _o) {
			if (this == _o) return true;
			if (_o == null || getClass() != _o.getClass()) return false;
			MeterMinute that = (MeterMinute) _o;
			return meter == that.meter && minute.equals(that.minute);
		}

		@Override
		public int hashCode() {
			return Objects.hash(meter, minute);
		}
	}

	private static class MeterMinuteValues {
		public double[] usage = new double[60];
		public double[] solar = new double[60];
		public double[] charges = new double[60];
	}
}
