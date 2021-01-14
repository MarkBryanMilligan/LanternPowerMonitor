package com.lanternsoftware.datamodel.zwave.dao;

import com.lanternsoftware.datamodel.zwave.Switch;
import com.lanternsoftware.datamodel.zwave.ZWaveConfig;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;

import java.util.Collections;
import java.util.List;

public class ZWaveConfigSerializer extends AbstractDaoSerializer<ZWaveConfig>
{
	@Override
	public Class<ZWaveConfig> getSupportedClass()
	{
		return ZWaveConfig.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(ZWaveConfig _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("_id", String.valueOf(_o.getAccountId()));
		d.put("switches", DaoSerializer.toDaoEntities(_o.getSwitches(), DaoProxyType.MONGO));
		return d;
	}

	@Override
	public ZWaveConfig fromDaoEntity(DaoEntity _d)
	{
		ZWaveConfig o = new ZWaveConfig();
		o.setAccountId(DaoSerializer.getInteger(_d, "_id"));
		o.setSwitches(DaoSerializer.getList(_d, "switches", Switch.class));
		return o;
	}
}