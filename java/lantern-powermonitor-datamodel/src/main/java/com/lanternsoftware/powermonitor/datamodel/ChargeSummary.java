package com.lanternsoftware.powermonitor.datamodel;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

@DBSerializable(autogen = false)
public class ChargeSummary {
	private int accountId;
	private int planId;
	private String groupId;
	private String groupName;
	private EnergyViewMode viewMode;
	private Date start;
	private List<ChargeSummary> subGroups;
	private TimeZone timezone;
	private double[] charges;
	private double totalUsageJoules;
	private double totalSolarJoules;
	private double fromGridJoules;
	private double toGridJoules;
	private double peakToGrid;
	private double peakFromGrid;
	private double peakConsumption;
	private double peakProduction;

	public ChargeSummary() {
	}

	public ChargeSummary(int _accountId, int _planId, String _groupId, String _groupName, EnergyViewMode _viewMode, Date _start, TimeZone _timezone) {
		accountId = _accountId;
		planId = _planId;
		groupId = _groupId;
		groupName = _groupName;
		viewMode = _viewMode;
		start = _start;
		timezone = _timezone;
	}

	public ChargeSummary(BreakerGroup _group, BillingPlan _plan, Map<String, List<ChargeTotal>> _charges, EnergyViewMode _viewMode, Date _start, TimeZone _timezone) {
		this(_group.getAccountId(), _plan.getPlanId(), _group.getId(), _group.getName(), _viewMode, _start, _timezone);
		subGroups = CollectionUtils.transform(_group.getSubGroups(), _g -> new ChargeSummary(_g, _plan, _charges, _viewMode, _start, _timezone));
		if (_viewMode == EnergyViewMode.MONTH) {
			charges = new double[DateUtils.getDaysBetween(_start, _plan.getBillingCycleEnd(_start, _timezone), _timezone)];
		}
		else
			charges = new double[_viewMode.blockCount(_start, _timezone)];
		for (ChargeTotal charge : CollectionUtils.makeNotNull(_charges.get(_group.getId()))) {
			int idx;
			if (_viewMode == EnergyViewMode.MONTH)
				idx = DateUtils.getDaysBetween(_start, charge.getStart(), _timezone);
			else
				idx = viewMode.blockIndex(start, charge.getStart(), timezone);
			if (idx < charges.length)
				charges[idx] += charge.getCharge();
		}
		totalUsageJoules = CollectionUtils.sum(CollectionUtils.transform(_charges.get(_group.getId()), ChargeTotal::getTotalUsageJoules));
		totalSolarJoules = CollectionUtils.sum(CollectionUtils.transform(_charges.get(_group.getId()), ChargeTotal::getTotalSolarJoules));
		fromGridJoules = CollectionUtils.sum(CollectionUtils.transform(_charges.get(_group.getId()), ChargeTotal::getFromGridJoules));
		toGridJoules = CollectionUtils.sum(CollectionUtils.transform(_charges.get(_group.getId()), ChargeTotal::getToGridJoules));
		peakToGrid = DaoSerializer.toDouble(CollectionUtils.getLargest(CollectionUtils.transform(_charges.get(_group.getId()), ChargeTotal::getPeakToGrid)));
		peakFromGrid = DaoSerializer.toDouble(CollectionUtils.getLargest(CollectionUtils.transform(_charges.get(_group.getId()), ChargeTotal::getPeakFromGrid)));
		peakConsumption = DaoSerializer.toDouble(CollectionUtils.getLargest(CollectionUtils.transform(_charges.get(_group.getId()), ChargeTotal::getPeakConsumption)));
		peakProduction = DaoSerializer.toDouble(CollectionUtils.getLargest(CollectionUtils.transform(_charges.get(_group.getId()), ChargeTotal::getPeakProduction)));
	}

	public String getId() {
		return toId(accountId, planId, groupId, viewMode, start);
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public int getPlanId() {
		return planId;
	}

	public void setPlanId(int _planId) {
		planId = _planId;
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

	public List<ChargeSummary> getSubGroups() {
		return subGroups;
	}

	public void setSubGroups(List<ChargeSummary> _subGroups) {
		subGroups = _subGroups;
	}

	public double[] getCharges() {
		return charges;
	}

	public void setCharges(double[] _charges) {
		charges = _charges;
	}

	public TimeZone getTimezone() {
		return timezone;
	}

	public void setTimezone(TimeZone _timezone) {
		timezone = _timezone;
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

	public double charge() {
		return charge(null);
	}

	public double charge(Set<String> _selectedBreakers) {
		return charge(_selectedBreakers, true);
	}

	public double charge(Set<String> _selectedBreakers, GridFlow _mode) {
		return charge(_selectedBreakers, true, _mode);
	}

	public double charge(Set<String> _selectedBreakers, boolean _includeSubgroups) {
		return charge(_selectedBreakers, _includeSubgroups, null);
	}

	public double charge(Set<String> _selectedBreakers, boolean _includeSubgroups, GridFlow _mode) {
		double charge = 0.0;
		if (_includeSubgroups) {
			for (ChargeSummary group : CollectionUtils.makeNotNull(subGroups)) {
				charge += group.charge(_selectedBreakers, true, _mode);
			}
		}
		if ((charges != null) && ((_selectedBreakers == null) || _selectedBreakers.contains(getGroupId()))) {
			for (double c : charges) {
				if ((_mode == null) || ((_mode == GridFlow.TO) && c < 0.0) || (_mode == GridFlow.FROM && c > 0.0))
					charge += c;
			}
		}
		return charge;
	}

	public float[] chargeBlocks() {
		return chargeBlocks(null);
	}

	public float[] chargeBlocks(Set<String> _selectedGroups) {
		return chargeBlocks(_selectedGroups, GridFlow.BOTH);
	}

	public float[] chargeBlocks(Set<String> _selectedGroups, GridFlow _flow) {
		float[] blocks = new float[blockCount()];
		chargeBlocks(_selectedGroups, blocks, _flow);
		return blocks;
	}

	private void chargeBlocks(Set<String> _selectedGroups, float[] _chargeBlocks, GridFlow _flow) {
		if ((charges != null) && ((_selectedGroups == null) || _selectedGroups.contains(getGroupId()))) {
			for (int i = 0; i < charges.length; i++) {
				if ((_flow == GridFlow.BOTH) || ((_flow == GridFlow.FROM) && charges[i] >= 0.0) || ((_flow == GridFlow.TO) && charges[i] <= 0.0))
					_chargeBlocks[i] += charges[i];
			}
		}
		for (ChargeSummary group : CollectionUtils.makeNotNull(subGroups)) {
			group.chargeBlocks(_selectedGroups, _chargeBlocks, _flow);
		}
	}

	private int blockCount() {
		int blocks = 0;
		for (ChargeSummary s : CollectionUtils.makeNotNull(subGroups)) {
			blocks = Math.max(blocks, s.blockCount());
		}
		if ((charges != null) && (charges.length > blocks))
			return charges.length;
		return blocks;
	}

	public static String toId(int _accountId, int _planId, String _groupId, EnergyViewMode _viewMode, Date _start) {
		return _accountId + "-" + _planId + "-" + _groupId + "-" + DaoSerializer.toEnumName(_viewMode) + "-" + _start.getTime();
	}
}
