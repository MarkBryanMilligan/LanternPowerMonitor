package com.lanternsoftware.datamodel.currentmonitor.archive.dao;

import com.lanternsoftware.datamodel.currentmonitor.archive.DailyEnergyArchive;
import com.lanternsoftware.datamodel.currentmonitor.archive.MonthlyEnergyArchive;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class MonthlyEnergyArchiveSerializer extends AbstractDaoSerializer<MonthlyEnergyArchive>
{
	@Override
	public Class<MonthlyEnergyArchive> getSupportedClass()
	{
		return MonthlyEnergyArchive.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(MonthlyEnergyArchive _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("month", DaoSerializer.toLong(_o.getMonth()));
		d.put("days", DaoSerializer.toDaoEntities(_o.getDays(), DaoProxyType.MONGO));
		return d;
	}

	@Override
	public MonthlyEnergyArchive fromDaoEntity(DaoEntity _d)
	{
		MonthlyEnergyArchive o = new MonthlyEnergyArchive();
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setMonth(DaoSerializer.getDate(_d, "month"));
		o.setDays(DaoSerializer.getList(_d, "days", DailyEnergyArchive.class));
		return o;
	}
}