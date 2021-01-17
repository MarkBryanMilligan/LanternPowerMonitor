package com.lanternsoftware.datamodel.currentmonitor;


import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

	public BreakerGroupEnergy(BreakerGroup _group, Map<String, List<BreakerPower>> _powerReadings, EnergyBlockViewMode _viewMode, Date _start, TimeZone _timezone) {
		groupId = _group.getId();
		groupName = _group.getName();
		viewMode = _viewMode;
		start = _start;
		accountId = _group.getAccountId();
		timezone = _timezone;
		subGroups = CollectionUtils.transform(_group.getSubGroups(), _g -> new BreakerGroupEnergy(_g, _powerReadings, _viewMode, _start, timezone));
		energyBlocks = new ArrayList<>();
		List<String> breakerKeys = CollectionUtils.transform(_group.getBreakers(), Breaker::getKey);
		if (!breakerKeys.isEmpty()) {
			for (BreakerPower power : CollectionUtils.aggregate(breakerKeys, _powerReadings::get)) {
				addEnergy(groupId, power.getReadTime(), power.getPower());
			}
		}
	}

	public BreakerGroupEnergy(BreakerGroup _group, List<HubPowerMinute> _power, EnergyBlockViewMode _viewMode, Date _start, TimeZone _timezone) {
		groupId = _group.getId();
		groupName = _group.getName();
		viewMode = _viewMode;
		start = _start;
		accountId = _group.getAccountId();
		timezone = _timezone;
		subGroups = CollectionUtils.transform(_group.getSubGroups(), _g -> new BreakerGroupEnergy(_g, (List<HubPowerMinute>)null, _viewMode, _start, timezone));
		energyBlocks = new ArrayList<>();
		addEnergy(_group, _power);
	}

	public void addEnergy(BreakerGroup _group, List<HubPowerMinute> _hubPower) {
		Map<String, Breaker> breakers = CollectionUtils.transformToMap(_group.getAllBreakers(), Breaker::getKey);
		Map<String, BreakerGroup> breakerKeyToGroup = new HashMap<>();
		for (BreakerGroup group : _group.getAllBreakerGroups()) {
			for (Breaker b : group.getAllBreakers()) {
				breakerKeyToGroup.put(b.getKey(), group);
			}
		}
		addEnergy(breakers, breakerKeyToGroup, _hubPower);
	}

	public void addEnergy(Map<String, Breaker> _breakers, Map<String, BreakerGroup> _breakerKeyToGroup, List<HubPowerMinute> _hubPower) {
		if (CollectionUtils.isEmpty(_hubPower) || CollectionUtils.anyQualify(_hubPower, _p->_p.getAccountId() != accountId))
			return;
		Date minute = CollectionUtils.getFirst(_hubPower).getMinuteAsDate();
		resetEnergy(minute);
		Map<Integer, MeterMinute> meters = new HashMap<>();
		for (HubPowerMinute hubPower : _hubPower) {
			for (BreakerPowerMinute breaker : CollectionUtils.makeNotNull(hubPower.getBreakers())) {
				Breaker b = _breakers.get(breaker.breakerKey());
				if (b == null)
					continue;
				BreakerGroup group = _breakerKeyToGroup.get(breaker.breakerKey());
				if (group == null)
					continue;
				MeterMinute meter = meters.computeIfAbsent(b.getMeter(), _p->new MeterMinute());
				int idx = 0;
				for (Float power : CollectionUtils.makeNotNull(breaker.getReadings())) {
					if (power > 0)
						meter.usage[idx] += power;
					else
						meter.solar[idx] += -power;
					if (power != 0.0)
						addEnergy(group.getId(), minute, power);
					idx++;
				}
			}
		}

		for (MeterMinute meter : meters.values()) {
			for (int i = 0; i < 60; i++) {
				if (meter.usage[i] > meter.solar[i])
					fromGrid += meter.usage[i] - meter.solar[i];
				else
					toGrid += meter.solar[i] - meter.usage[i];
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

	public void addEnergy(String _groupId, Date _readTime, double _joules) {
		if (NullUtils.isEqual(groupId, _groupId))
			getBlock(_readTime).addJoules(_joules);
		else {
			for (BreakerGroupEnergy subGroup : CollectionUtils.makeNotNull(subGroups)) {
				subGroup.addEnergy(_groupId, _readTime, _joules);
			}
		}
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
		int idx = viewMode.blockIndex(_readTime, timezone);
		if (_add && (idx >= size)) {
			if (energyBlocks == null)
				energyBlocks = new ArrayList<>();
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
				}
			}
		}
		for (BreakerGroupEnergy group : CollectionUtils.makeNotNull(subGroups)) {
			group.getAllEnergyBlocks(_selectedGroups, _energyBlocks, _type);
		}
	}

	private static class MeterMinute {
		public double[] usage = new double[60];
		public double[] solar = new double[60];
	}
}
