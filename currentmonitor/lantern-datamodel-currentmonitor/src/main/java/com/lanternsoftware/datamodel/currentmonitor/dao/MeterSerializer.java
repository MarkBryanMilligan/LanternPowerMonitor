package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.Meter;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class MeterSerializer extends AbstractDaoSerializer<Meter>
{
	@Override
	public Class<Meter> getSupportedClass()
	{
		return Meter.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(Meter _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("account_id", _o.getAccountId());
		d.put("index", _o.getIndex());
		d.put("name", _o.getName());
		return d;
	}

	@Override
	public Meter fromDaoEntity(DaoEntity _d)
	{
		Meter o = new Meter();
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setIndex(DaoSerializer.getInteger(_d, "index"));
		o.setName(DaoSerializer.getString(_d, "name"));
		return o;
	}
}