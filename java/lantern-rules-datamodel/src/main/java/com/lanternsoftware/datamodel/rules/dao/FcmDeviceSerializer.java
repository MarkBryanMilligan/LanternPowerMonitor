package com.lanternsoftware.datamodel.rules.dao;

import com.lanternsoftware.datamodel.rules.FcmDevice;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class FcmDeviceSerializer extends AbstractDaoSerializer<FcmDevice>
{
	@Override
	public Class<FcmDevice> getSupportedClass()
	{
		return FcmDevice.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(FcmDevice _o)
	{
		DaoEntity d = new DaoEntity();
		if (_o.getId() != null)
			d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("token", _o.getToken());
		d.put("name", _o.getName());
		d.put("posted", DaoSerializer.toLong(_o.getPosted()));
		return d;
	}

	@Override
	public FcmDevice fromDaoEntity(DaoEntity _d)
	{
		FcmDevice o = new FcmDevice();
		o.setId(DaoSerializer.getString(_d, "_id"));
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setToken(DaoSerializer.getString(_d, "token"));
		o.setName(DaoSerializer.getString(_d, "name"));
		o.setPosted(DaoSerializer.getDate(_d, "posted"));
		return o;
	}
}