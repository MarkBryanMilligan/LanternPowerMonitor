package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.EnergyBlock;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class EnergyBlockSerializer extends AbstractDaoSerializer<EnergyBlock>
{
	@Override
	public Class<EnergyBlock> getSupportedClass()
	{
		return EnergyBlock.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(EnergyBlock _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("start", DaoSerializer.toLong(_o.getStart()));
		d.put("end", DaoSerializer.toLong(_o.getEnd()));
		d.put("joules", _o.getJoules());
		d.put("charge", _o.getCharge());
		return d;
	}

	@Override
	public EnergyBlock fromDaoEntity(DaoEntity _d)
	{
		EnergyBlock o = new EnergyBlock();
		o.setStart(DaoSerializer.getDate(_d, "start"));
		o.setEnd(DaoSerializer.getDate(_d, "end"));
		o.setJoules(DaoSerializer.getDouble(_d, "joules"));
		o.setCharge(DaoSerializer.getDouble(_d, "charge"));
		return o;
	}
}