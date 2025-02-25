package com.lanternsoftware.powermonitor.datamodel.archive.dao;

import com.lanternsoftware.powermonitor.datamodel.archive.BreakerEnergyArchive;
import com.lanternsoftware.powermonitor.datamodel.archive.DailyEnergyArchive;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class DailyEnergyArchiveSerializer extends AbstractDaoSerializer<DailyEnergyArchive>
{
	@Override
	public Class<DailyEnergyArchive> getSupportedClass()
	{
		return DailyEnergyArchive.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(DailyEnergyArchive _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("breakers", DaoSerializer.toDaoEntities(_o.getBreakers(), DaoProxyType.MONGO));
		return d;
	}

	@Override
	public DailyEnergyArchive fromDaoEntity(DaoEntity _d)
	{
		DailyEnergyArchive o = new DailyEnergyArchive();
		o.setBreakers(DaoSerializer.getList(_d, "breakers", BreakerEnergyArchive.class));
		return o;
	}
}