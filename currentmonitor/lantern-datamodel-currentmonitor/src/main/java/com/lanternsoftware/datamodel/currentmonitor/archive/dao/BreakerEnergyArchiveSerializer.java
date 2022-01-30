package com.lanternsoftware.datamodel.currentmonitor.archive.dao;

import com.lanternsoftware.datamodel.currentmonitor.archive.BreakerEnergyArchive;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class BreakerEnergyArchiveSerializer extends AbstractDaoSerializer<BreakerEnergyArchive>
{
	@Override
	public Class<BreakerEnergyArchive> getSupportedClass()
	{
		return BreakerEnergyArchive.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(BreakerEnergyArchive _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("panel", _o.getPanel());
		d.put("space", _o.getSpace());
		d.put("readings", _o.getReadings());
		return d;
	}

	@Override
	public BreakerEnergyArchive fromDaoEntity(DaoEntity _d)
	{
		BreakerEnergyArchive o = new BreakerEnergyArchive();
		o.setPanel(DaoSerializer.getInteger(_d, "panel"));
		o.setSpace(DaoSerializer.getInteger(_d, "space"));
		o.setReadings(DaoSerializer.getByteArray(_d, "readings"));
		return o;
	}
}