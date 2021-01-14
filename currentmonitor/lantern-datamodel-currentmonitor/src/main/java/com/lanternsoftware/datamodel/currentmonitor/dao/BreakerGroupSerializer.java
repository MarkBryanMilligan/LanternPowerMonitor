package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroup;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class BreakerGroupSerializer extends AbstractDaoSerializer<BreakerGroup>
{
	@Override
	public Class<BreakerGroup> getSupportedClass()
	{
		return BreakerGroup.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(BreakerGroup _o)
	{
		DaoEntity d = new DaoEntity();
		if (_o.getId() != null)
			d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("name", _o.getName());
		d.put("sub_groups", DaoSerializer.toDaoEntities(_o.getSubGroups(), DaoProxyType.MONGO));
		d.put("breakers", DaoSerializer.toDaoEntities(_o.getBreakers(), DaoProxyType.MONGO));
		return d;
	}

	@Override
	public BreakerGroup fromDaoEntity(DaoEntity _d)
	{
		BreakerGroup o = new BreakerGroup();
		o.setId(DaoSerializer.getString(_d, "_id"));
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setName(DaoSerializer.getString(_d, "name"));
		o.setSubGroups(DaoSerializer.getList(_d, "sub_groups", BreakerGroup.class));
		o.setBreakers(DaoSerializer.getList(_d, "breakers", Breaker.class));
		return o;
	}
}