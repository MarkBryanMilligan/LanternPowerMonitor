package com.lanternsoftware.datamodel.currentmonitor;


import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@DBSerializable(autogen = false)
public class ChargeTotal {
	private int accountId;
	private String groupId;
	private int planId;
	private EnergyViewMode viewMode;
	private Date start;
	private double charge;
	private List<ChargeTotal> subGroups;
	private double totalUsageJoules;
	private double totalSolarJoules;
	private double fromGridJoules;
	private double toGridJoules;
	private double peakToGrid;
	private double peakFromGrid;
	private double peakConsumption;
	private double peakProduction;

	public ChargeTotal() {
	}

	public ChargeTotal(ChargeSummary _summary) {
		accountId = _summary.getAccountId();
		groupId = _summary.getGroupId();
		planId = _summary.getPlanId();
		viewMode = _summary.getViewMode();
		start = _summary.getStart();
		if (_summary.getCharges() != null) {
			for (double c : _summary.getCharges()) {
				charge += c;
			}
		}
		subGroups = CollectionUtils.transform(_summary.getSubGroups(), ChargeTotal::new);
		totalUsageJoules = _summary.getTotalUsageJoules();
		totalSolarJoules = _summary.getTotalSolarJoules();
		fromGridJoules = _summary.getFromGridJoules();
		toGridJoules = _summary.getToGridJoules();
		peakToGrid = _summary.getPeakToGrid();
		peakFromGrid = _summary.getPeakFromGrid();
		peakConsumption = _summary.getPeakConsumption();
		peakProduction = _summary.getPeakProduction();
	}

	public String getId() {
		return ChargeSummary.toId(accountId, planId, groupId, viewMode, start);
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

	public int getPlanId() {
		return planId;
	}

	public void setPlanId(int _planId) {
		planId = _planId;
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

	public double getCharge() {
		return charge;
	}

	public void setCharge(double _charge) {
		charge = _charge;
	}

	public List<ChargeTotal> getSubGroups() {
		return subGroups;
	}

	public void setSubGroups(List<ChargeTotal> _subGroups) {
		subGroups = _subGroups;
	}

	public double getTotalUsageJoules() {
		return totalUsageJoules;
	}

	public void setTotalUsageJoules(double _totalUsageJoules) {
		totalUsageJoules = _totalUsageJoules;
	}

	public double getTotalSolarJoules() {
		return totalSolarJoules;
	}

	public void setTotalSolarJoules(double _totalSolarJoules) {
		totalSolarJoules = _totalSolarJoules;
	}

	public double getFromGridJoules() {
		return fromGridJoules;
	}

	public void setFromGridJoules(double _fromGridJoules) {
		fromGridJoules = _fromGridJoules;
	}

	public double getToGridJoules() {
		return toGridJoules;
	}

	public void setToGridJoules(double _toGridJoules) {
		toGridJoules = _toGridJoules;
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

	public double chargeTotal() {
		double retCharge = charge;
		for (ChargeTotal t : CollectionUtils.makeNotNull(subGroups)) {
			retCharge += t.chargeTotal();
		}
		return retCharge;
	}

	public List<ChargeTotal> flatten() {
		List<ChargeTotal> totals = new ArrayList<>();
		flatten(totals);
		return totals;
	}

	private void flatten(List<ChargeTotal> _totals) {
		_totals.add(this);
		for (ChargeTotal total : CollectionUtils.makeNotNull(subGroups)) {
			total.flatten(_totals);
		}
	}
}
