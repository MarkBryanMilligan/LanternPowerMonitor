package com.lanternsoftware.datamodel.currentmonitor;


import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@DBSerializable(autogen = false)
public class EnergyTotal {
	private int accountId;
	private String groupId;
	private EnergyViewMode viewMode;
	private Date start;
	private List<EnergyTotal> subGroups;
	private double joules;
	private double flow;
	private double peakToGrid;
	private double peakFromGrid;
	private double peakConsumption;
	private double peakProduction;

	public EnergyTotal() {
	}

	public EnergyTotal(EnergySummary _energy) {
		accountId = _energy.getAccountId();
		groupId = _energy.getGroupId();
		viewMode = _energy.getViewMode();
		start = _energy.getStart();
		subGroups = CollectionUtils.transform(_energy.getSubGroups(), EnergyTotal::new);
		joules = _energy.joules(null, false, GridFlow.BOTH);
		flow = _energy.flow(null, false, GridFlow.BOTH);
		peakToGrid = _energy.getPeakToGrid();
		peakFromGrid = _energy.getPeakFromGrid();
		peakConsumption = _energy.getPeakConsumption();
		peakProduction = _energy.getPeakProduction();
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

	public EnergyTotal getSubGroup(String _groupId) {
		return CollectionUtils.filterOne(subGroups, _g->_groupId.equals(_g.getGroupId()));
	}

	public List<EnergyTotal> getSubGroups() {
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

	public void setSubGroups(List<EnergyTotal> _subGroups) {
		subGroups = _subGroups;
	}

	public double getJoules() {
		return joules;
	}

	public void setJoules(double _joules) {
		joules = _joules;
	}

	public double getFlow() {
		return flow;
	}

	public void setFlow(double _flow) {
		flow = _flow;
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

	public double totalJoules() {
		double total = joules;
		for (EnergyTotal t : CollectionUtils.makeNotNull(subGroups)) {
			total += t.totalJoules();
		}
		return total;
	}

	public List<EnergyTotal> flatten() {
		List<EnergyTotal> totals = new ArrayList<>();
		flatten(totals);
		return totals;
	}

	private void flatten(List<EnergyTotal> _totals) {
		_totals.add(this);
		for (EnergyTotal total : CollectionUtils.makeNotNull(subGroups)) {
			total.flatten(_totals);
		}
	}
}
