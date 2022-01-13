package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.HubCommand;
import com.lanternsoftware.datamodel.currentmonitor.HubConfigCharacteristic;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class HubCommandSerializer extends AbstractDaoSerializer<HubCommand>
{
	@Override
	public Class<HubCommand> getSupportedClass()
	{
		return HubCommand.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(HubCommand _o)
	{
		DaoEntity d = new DaoEntity();
		if (_o.getId() != null)
			d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("hub", _o.getHub());
		d.put("created", DaoSerializer.toLong(_o.getCreated()));
		d.put("characteristic", DaoSerializer.toEnumName(_o.getCharacteristic()));
		d.put("data", _o.getData());
		return d;
	}

	@Override
	public HubCommand fromDaoEntity(DaoEntity _d)
	{
		HubCommand o = new HubCommand();
		o.setId(DaoSerializer.getString(_d, "_id"));
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setHub(DaoSerializer.getInteger(_d, "hub"));
		o.setCreated(DaoSerializer.getDate(_d, "created"));
		o.setCharacteristic(DaoSerializer.getEnum(_d, "characteristic", HubConfigCharacteristic.class));
		o.setData(DaoSerializer.getByteArray(_d, "data"));
		return o;
	}
}