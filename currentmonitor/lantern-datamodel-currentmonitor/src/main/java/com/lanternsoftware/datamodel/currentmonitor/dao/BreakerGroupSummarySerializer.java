package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.BreakerGroupSummary;
import com.lanternsoftware.datamodel.currentmonitor.EnergyBlockViewMode;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;

import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class BreakerGroupSummarySerializer extends AbstractDaoSerializer<BreakerGroupSummary> {
	@Override
	public Class<BreakerGroupSummary> getSupportedClass() {
		return BreakerGroupSummary.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(BreakerGroupSummary _o) {
		DaoEntity d = new DaoEntity();
		d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("group_id", _o.getGroupId());
		d.put("group_name", _o.getGroupName());
		d.put("view_mode", DaoSerializer.toEnumName(_o.getViewMode()));
		d.put("start", DaoSerializer.toLong(_o.getStart()));
		d.put("sub_groups", DaoSerializer.toDaoEntities(_o.getSubGroups(), DaoProxyType.MONGO));
		d.put("joules", _o.getJoules());
		d.put("charge", _o.getCharge());
		d.put("to_grid", _o.getToGrid());
		d.put("from_grid", _o.getFromGrid());
		d.put("peak_to_grid", _o.getPeakToGrid());
		d.put("peak_from_grid", _o.getPeakFromGrid());
		d.put("peak_production", _o.getPeakProduction());
		d.put("peak_consumption", _o.getPeakConsumption());
		return d;
	}

	@Override
	public BreakerGroupSummary fromDaoEntity(DaoEntity _d) {
		BreakerGroupSummary o = new BreakerGroupSummary();
		o.setGroupId(DaoSerializer.getString(_d, "group_id"));
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setGroupName(DaoSerializer.getString(_d, "group_name"));
		o.setViewMode(DaoSerializer.getEnum(_d, "view_mode", EnergyBlockViewMode.class));
		o.setStart(DaoSerializer.getDate(_d, "start"));
		o.setSubGroups(DaoSerializer.getList(_d, "sub_groups", BreakerGroupSummary.class));
		o.setJoules(DaoSerializer.getDouble(_d, "joules"));
		o.setCharge(DaoSerializer.getDouble(_d, "charge"));
		o.setToGrid(DaoSerializer.getDouble(_d, "to_grid"));
		o.setFromGrid(DaoSerializer.getDouble(_d, "from_grid"));
		o.setPeakToGrid(DaoSerializer.getDouble(_d, "peak_to_grid"));
		o.setPeakFromGrid(DaoSerializer.getDouble(_d, "peak_from_grid"));
		o.setPeakProduction(DaoSerializer.getDouble(_d, "peak_production"));
		o.setPeakConsumption(DaoSerializer.getDouble(_d, "peak_consumption"));
		return o;
	}
}