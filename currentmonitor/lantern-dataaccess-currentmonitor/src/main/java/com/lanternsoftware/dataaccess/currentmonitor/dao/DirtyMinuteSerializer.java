package com.lanternsoftware.dataaccess.currentmonitor.dao;

import com.lanternsoftware.dataaccess.currentmonitor.DirtyMinute;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class DirtyMinuteSerializer extends AbstractDaoSerializer<DirtyMinute>
{
	@Override
	public Class<DirtyMinute> getSupportedClass()
	{
		return DirtyMinute.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(DirtyMinute _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("minute", _o.getMinute());
		d.put("posted", DaoSerializer.toLong(_o.getPosted()));
		return d;
	}

	@Override
	public DirtyMinute fromDaoEntity(DaoEntity _d)
	{
		DirtyMinute o = new DirtyMinute();
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setMinute(DaoSerializer.getInteger(_d, "minute"));
		o.setPosted(DaoSerializer.getDate(_d, "posted"));
		return o;
	}
}