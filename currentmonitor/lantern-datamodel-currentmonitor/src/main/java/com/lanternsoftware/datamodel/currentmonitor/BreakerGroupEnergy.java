package com.lanternsoftware.datamodel.currentmonitor;


import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.mutable.MutableDouble;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@DBSerializable(autogen = false)
public class BreakerGroupEnergy {
    private int accountId;
    private String groupId;
    private String groupName;
    private EnergyViewMode viewMode;
    private Date start;
    private List<BreakerGroupEnergy> subGroups;
    private List<EnergyBlock> energyBlocks;
    private double toGrid;
    private double fromGrid;
    private double peakToGrid;
    private double peakFromGrid;
    private double peakConsumption;
    private double peakProduction;
    private TimeZone timezone;

    public BreakerGroupEnergy() {
    }

    public BreakerGroupEnergy(EnergySummary _summary, List<BillingRate> _rates, Map<String, Integer> _breakerGroupMeters) {
        groupId = _summary.getGroupId();
        groupName = _summary.getGroupName();
        viewMode = _summary.getViewMode();
        start = _summary.getStart();
        accountId = _summary.getAccountId();
        timezone = _summary.getTimeZone();
        peakToGrid = _summary.getPeakToGrid();
        peakFromGrid = _summary.getPeakFromGrid();
        peakConsumption = _summary.getPeakConsumption();
        peakProduction = _summary.getPeakProduction();
        Date readTime = start;
        if (_summary.getEnergy() != null) {
            for (float joules : _summary.getEnergy()) {
                addEnergy(readTime, joules);
                readTime = viewMode.incrementBlock(readTime, timezone);
            }
        }
        readTime = start;
        ChargeSummary charges = _summary.toChargeSummary(0, _rates, _breakerGroupMeters, new MutableDouble(0.0));
        for (double charge : charges.chargeBlocks(CollectionUtils.asHashSet(groupId), GridFlow.BOTH)) {
            addCharge(readTime, charge);
            readTime = viewMode.incrementBlock(readTime, timezone);
        }
        subGroups = CollectionUtils.transform(_summary.getSubGroups(), _e->new BreakerGroupEnergy(_e, _rates, _breakerGroupMeters));
    }

    private void addEnergy(Date _readTime, double _joules) {
        EnergyBlock block = getBlock(_readTime);
        if (block != null)
            block.addJoules(_joules);
    }

    private void addCharge(Date _readTime, double _charge) {
        EnergyBlock block = getBlock(_readTime);
        if (block != null)
            block.addCharge(_charge);
    }

    private EnergyBlock getBlock(Date _readTime) {
        int size = CollectionUtils.size(energyBlocks);
        int idx = viewMode.blockIndex(start, _readTime, timezone);
        if (idx >= size) {
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

    public BreakerGroupEnergy getSubGroup(String _groupId) {
        return CollectionUtils.filterOne(subGroups, _g -> _groupId.equals(_g.getGroupId()));
    }

    public List<BreakerGroupEnergy> getSubGroups() {
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
}
