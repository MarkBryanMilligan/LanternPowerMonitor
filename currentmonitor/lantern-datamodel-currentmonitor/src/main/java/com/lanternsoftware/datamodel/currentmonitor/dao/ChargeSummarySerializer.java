package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.ChargeSummary;
import com.lanternsoftware.datamodel.currentmonitor.EnergyViewMode;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class ChargeSummarySerializer extends AbstractDaoSerializer<ChargeSummary>
{
	@Override
	public Class<ChargeSummary> getSupportedClass()
	{
		return ChargeSummary.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(ChargeSummary _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("plan_id", _o.getPlanId());
		d.put("group_id", _o.getGroupId());
		d.put("group_name", _o.getGroupName());
		d.put("view_mode", DaoSerializer.toEnumName(_o.getViewMode()));
		d.put("start", DaoSerializer.toLong(_o.getStart()));
		d.put("sub_groups", DaoSerializer.toDaoEntities(_o.getSubGroups(), DaoProxyType.MONGO));
		TimeZone tz = DateUtils.defaultTimeZone(_o.getTimezone());
		d.put("timezone", tz.getID());
		if (_o.getCharges() != null)
			d.put("charges", CollectionUtils.toByteArray(_o.getCharges()));
		d.put("total_usage_joules", _o.getTotalUsageJoules());
		d.put("total_solar_joules", _o.getTotalSolarJoules());
		d.put("from_grid_joules", _o.getFromGridJoules());
		d.put("to_grid_joules", _o.getToGridJoules());
		d.put("peak_to_grid", _o.getPeakToGrid());
		d.put("peak_from_grid", _o.getPeakFromGrid());
		d.put("peak_production", _o.getPeakProduction());
		d.put("peak_consumption", _o.getPeakConsumption());
		return d;
	}

	@Override
	public ChargeSummary fromDaoEntity(DaoEntity _d)
	{
		ChargeSummary o = new ChargeSummary();
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setPlanId(DaoSerializer.getInteger(_d, "plan_id"));
		o.setGroupId(DaoSerializer.getString(_d, "group_id"));
		o.setGroupName(DaoSerializer.getString(_d, "group_name"));
		o.setViewMode(DaoSerializer.getEnum(_d, "view_mode", EnergyViewMode.class));
		o.setStart(DaoSerializer.getDate(_d, "start"));
		o.setSubGroups(DaoSerializer.getList(_d, "sub_groups", ChargeSummary.class));
		o.setTimezone(DateUtils.fromTimeZoneId(DaoSerializer.getString(_d, "timezone")));
		o.setCharges(CollectionUtils.toDoubleArray(DaoSerializer.getByteArray(_d, "charges")));
		o.setTotalUsageJoules(DaoSerializer.getDouble(_d, "total_usage_joules"));
		o.setTotalSolarJoules(DaoSerializer.getDouble(_d, "total_solar_joules"));
		o.setFromGridJoules(DaoSerializer.getDouble(_d, "from_grid_joules"));
		o.setToGridJoules(DaoSerializer.getDouble(_d, "to_grid_joules"));
		o.setPeakToGrid(DaoSerializer.getDouble(_d, "peak_to_grid"));
		o.setPeakFromGrid(DaoSerializer.getDouble(_d, "peak_from_grid"));
		o.setPeakProduction(DaoSerializer.getDouble(_d, "peak_production"));
		o.setPeakConsumption(DaoSerializer.getDouble(_d, "peak_consumption"));
		return o;
	}
}