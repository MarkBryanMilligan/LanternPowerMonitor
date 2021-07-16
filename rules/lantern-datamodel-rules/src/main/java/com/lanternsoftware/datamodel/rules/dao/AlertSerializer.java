package com.lanternsoftware.datamodel.rules.dao;

import com.lanternsoftware.datamodel.rules.Alert;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class AlertSerializer extends AbstractDaoSerializer<Alert>
{
	@Override
	public Class<Alert> getSupportedClass()
	{
		return Alert.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(Alert _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("message", _o.getMessage());
		return d;
	}

	@Override
	public Alert fromDaoEntity(DaoEntity _d)
	{
		Alert o = new Alert();
		o.setMessage(DaoSerializer.getString(_d, "message"));
		return o;
	}
}