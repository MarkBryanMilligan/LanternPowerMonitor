package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.ChargeTotal;
import com.lanternsoftware.datamodel.currentmonitor.EnergyViewMode;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class ChargeTotalSerializer extends AbstractDaoSerializer<ChargeTotal>
{
	@Override
	public Class<ChargeTotal> getSupportedClass()
	{
		return ChargeTotal.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(ChargeTotal _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("group_id", _o.getGroupId());
		d.put("plan_id", _o.getPlanId());
		d.put("view_mode", DaoSerializer.toEnumName(_o.getViewMode()));
		d.put("start", DaoSerializer.toLong(_o.getStart()));
		d.put("charge", _o.getCharge());
		d.put("sub_groups", DaoSerializer.toDaoEntities(_o.getSubGroups(), DaoProxyType.MONGO));
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
	public ChargeTotal fromDaoEntity(DaoEntity _d)
	{
		ChargeTotal o = new ChargeTotal();
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setGroupId(DaoSerializer.getString(_d, "group_id"));
		o.setPlanId(DaoSerializer.getInteger(_d, "plan_id"));
		o.setViewMode(DaoSerializer.getEnum(_d, "view_mode", EnergyViewMode.class));
		o.setStart(DaoSerializer.getDate(_d, "start"));
		o.setCharge(DaoSerializer.getDouble(_d, "charge"));
		o.setSubGroups(DaoSerializer.getList(_d, "sub_groups", ChargeTotal.class));
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