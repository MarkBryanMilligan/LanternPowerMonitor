package com.lanternsoftware.datamodel.rules.dao;

import com.lanternsoftware.datamodel.rules.Event;
import com.lanternsoftware.datamodel.rules.EventType;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class EventSerializer extends AbstractDaoSerializer<Event>
{
	@Override
	public Class<Event> getSupportedClass()
	{
		return Event.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(Event _o)
	{
		DaoEntity d = new DaoEntity();
		if (_o.getId() != null)
			d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("type", DaoSerializer.toEnumName(_o.getType()));
		d.put("time", DaoSerializer.toLong(_o.getTime()));
		d.put("event_description", _o.getEventDescription());
		d.put("source_id", _o.getSourceId());
		d.put("value", _o.getValue());
		return d;
	}

	@Override
	public Event fromDaoEntity(DaoEntity _d)
	{
		Event o = new Event();
		o.setId(DaoSerializer.getString(_d, "_id"));
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setType(DaoSerializer.getEnum(_d, "type", EventType.class));
		o.setTime(DaoSerializer.getDate(_d, "time"));
		o.setEventDescription(DaoSerializer.getString(_d, "event_description"));
		o.setSourceId(DaoSerializer.getString(_d, "source_id"));
		o.setValue(DaoSerializer.getDouble(_d, "value"));
		return o;
	}
}