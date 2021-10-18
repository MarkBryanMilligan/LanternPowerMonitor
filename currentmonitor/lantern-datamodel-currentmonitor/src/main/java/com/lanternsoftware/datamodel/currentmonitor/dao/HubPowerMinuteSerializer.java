package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.BreakerPowerMinute;
import com.lanternsoftware.datamodel.currentmonitor.HubPowerMinute;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class HubPowerMinuteSerializer extends AbstractDaoSerializer<HubPowerMinute>
{
	@Override
	public Class<HubPowerMinute> getSupportedClass()
	{
		return HubPowerMinute.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(HubPowerMinute _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("hub", _o.getHub());
		d.put("minute", _o.getMinute());
		d.put("breakers", DaoSerializer.toDaoEntities(_o.getBreakers(), DaoProxyType.MONGO));
		return d;
	}

	@Override
	public HubPowerMinute fromDaoEntity(DaoEntity _d)
	{
		HubPowerMinute o = new HubPowerMinute();
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setHub(DaoSerializer.getInteger(_d, "hub"));
		o.setMinute(DaoSerializer.getInteger(_d, "minute"));
		o.setBreakers(DaoSerializer.getList(_d, "breakers", BreakerPowerMinute.class));
		return o;
	}
}