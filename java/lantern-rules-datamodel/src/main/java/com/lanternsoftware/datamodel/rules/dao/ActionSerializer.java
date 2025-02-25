package com.lanternsoftware.datamodel.rules.dao;

import com.lanternsoftware.datamodel.rules.Action;
import com.lanternsoftware.datamodel.rules.ActionType;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class ActionSerializer extends AbstractDaoSerializer<Action>
{
	@Override
	public Class<Action> getSupportedClass()
	{
		return Action.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(Action _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("type", DaoSerializer.toEnumName(_o.getType()));
		d.put("description", _o.getDescription());
		d.put("destination_id", _o.getDestinationId());
		d.put("value", _o.getValue());
		return d;
	}

	@Override
	public Action fromDaoEntity(DaoEntity _d)
	{
		Action o = new Action();
		o.setType(DaoSerializer.getEnum(_d, "type", ActionType.class));
		o.setDescription(DaoSerializer.getString(_d, "description"));
		o.setDestinationId(DaoSerializer.getString(_d, "destination_id"));
		o.setValue(DaoSerializer.getDouble(_d, "value"));
		return o;
	}
}