package com.lanternsoftware.datamodel.currentmonitor.hub.dao;

import com.lanternsoftware.datamodel.currentmonitor.hub.BreakerSample;
import com.lanternsoftware.datamodel.currentmonitor.hub.HubSample;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class HubSampleSerializer extends AbstractDaoSerializer<HubSample>
{
	@Override
	public Class<HubSample> getSupportedClass()
	{
		return HubSample.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(HubSample _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("sample_date", DaoSerializer.toLong(_o.getSampleDate()));
		d.put("breakers", DaoSerializer.toDaoEntities(_o.getBreakers(), DaoProxyType.MONGO));
		return d;
	}

	@Override
	public HubSample fromDaoEntity(DaoEntity _d)
	{
		HubSample o = new HubSample();
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setSampleDate(DaoSerializer.getDate(_d, "sample_date"));
		o.setBreakers(DaoSerializer.getList(_d, "breakers", BreakerSample.class));
		return o;
	}
}