package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.EnergySummary;
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

public class EnergySummarySerializer extends AbstractDaoSerializer<EnergySummary>
{
	@Override
	public Class<EnergySummary> getSupportedClass()
	{
		return EnergySummary.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(EnergySummary _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("group_id", _o.getGroupId());
		d.put("group_name", _o.getGroupName());
		d.put("view_mode", DaoSerializer.toEnumName(_o.getViewMode()));
		d.put("start", DaoSerializer.toLong(_o.getStart()));
		d.put("sub_groups", DaoSerializer.toDaoEntities(_o.getSubGroups(), DaoProxyType.MONGO));
		d.put("main", _o.isMain());
		TimeZone tz = DateUtils.defaultTimeZone(_o.getTimeZone());
		d.put("timezone", tz.getID());
		if (_o.getEnergy() != null)
			d.put("energy", CollectionUtils.toByteArray(_o.getEnergy()));
		if (_o.getGridEnergy() != null)
			d.put("grid_energy", CollectionUtils.toByteArray(_o.getGridEnergy()));
		d.put("peak_to_grid", _o.getPeakToGrid());
		d.put("peak_from_grid", _o.getPeakFromGrid());
		d.put("peak_production", _o.getPeakProduction());
		d.put("peak_consumption", _o.getPeakConsumption());
		return d;
	}

	@Override
	public EnergySummary fromDaoEntity(DaoEntity _d)
	{
		EnergySummary o = new EnergySummary();
		o.setGroupId(DaoSerializer.getString(_d, "group_id"));
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setGroupName(DaoSerializer.getString(_d, "group_name"));
		o.setViewMode(DaoSerializer.getEnum(_d, "view_mode", EnergyViewMode.class));
		o.setStart(DaoSerializer.getDate(_d, "start"));
		o.setSubGroups(DaoSerializer.getList(_d, "sub_groups", EnergySummary.class));
		o.setMain(DaoSerializer.getBoolean(_d, "main"));
		o.setTimeZone(DateUtils.fromTimeZoneId(DaoSerializer.getString(_d, "timezone")));
		o.setEnergy(CollectionUtils.toFloatArray(DaoSerializer.getByteArray(_d, "energy")));
		o.setGridEnergy(CollectionUtils.toFloatArray(DaoSerializer.getByteArray(_d, "grid_energy")));
		o.setPeakToGrid(DaoSerializer.getDouble(_d, "peak_to_grid"));
		o.setPeakFromGrid(DaoSerializer.getDouble(_d, "peak_from_grid"));
		o.setPeakProduction(DaoSerializer.getDouble(_d, "peak_production"));
		o.setPeakConsumption(DaoSerializer.getDouble(_d, "peak_consumption"));
		return o;
	}
}