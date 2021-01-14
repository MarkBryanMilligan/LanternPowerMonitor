package com.lanternsoftware.datamodel.currentmonitor;


import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@DBSerializable(autogen = false)
public class BreakerGroupSummary {
	private int accountId;
	private String groupId;
	private String groupName;
	private EnergyBlockViewMode viewMode;
	private Date start;
	private List<BreakerGroupSummary> subGroups;
	private double joules;
	private double toGrid;
	private double fromGrid;

	public BreakerGroupSummary() {
	}

	public BreakerGroupSummary(BreakerGroupEnergy _energy) {
		accountId = _energy.getAccountId();
		groupId = _energy.getGroupId();
		groupName = _energy.getGroupName();
		viewMode = _energy.getViewMode();
		start = _energy.getStart();
		subGroups = CollectionUtils.transform(_energy.getSubGroups(), BreakerGroupSummary::new);
		joules = _energy.joules(null, false);
		toGrid = _energy.getToGrid();
		fromGrid = _energy.getFromGrid();
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

	public BreakerGroupSummary getSubGroup(String _groupId) {
		return CollectionUtils.filterOne(subGroups, _g->_groupId.equals(_g.getGroupId()));
	}

	public List<BreakerGroupSummary> getSubGroups() {
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

	public void setSubGroups(List<BreakerGroupSummary> _subGroups) {
		subGroups = _subGroups;
	}

	public double getJoules() {
		return joules;
	}

	public void setJoules(double _joules) {
		joules = _joules;
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

	public List<BreakerGroupSummary> getAllGroups() {
		Map<String, BreakerGroupSummary> groups = new TreeMap<>();
		getAllGroups(groups);
		return new ArrayList<>(groups.values());
	}

	public void getAllGroups(Map<String, BreakerGroupSummary> _groups) {
		if (NullUtils.isNotEmpty(getGroupId()))
			_groups.put(getGroupId(), this);
		for (BreakerGroupSummary group : CollectionUtils.makeNotNull(subGroups)) {
			group.getAllGroups(_groups);
		}
	}
}
