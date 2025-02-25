package com.lanternsoftware.powermonitor.datamodel.dao;

import com.lanternsoftware.powermonitor.datamodel.BreakerPanel;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class BreakerPanelSerializer extends AbstractDaoSerializer<BreakerPanel>
{
	@Override
	public Class<BreakerPanel> getSupportedClass()
	{
		return BreakerPanel.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(BreakerPanel _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("account_id", _o.getAccountId());
		d.put("name", _o.getName());
		d.put("index", _o.getIndex());
		d.put("spaces", _o.getSpaces());
		d.put("meter", _o.getMeter());
		return d;
	}

	@Override
	public BreakerPanel fromDaoEntity(DaoEntity _d)
	{
		BreakerPanel o = new BreakerPanel();
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setName(DaoSerializer.getString(_d, "name"));
		o.setIndex(DaoSerializer.getInteger(_d, "index"));
		o.setSpaces(DaoSerializer.getInteger(_d, "spaces"));
		o.setMeter(DaoSerializer.getInteger(_d, "meter"));
		return o;
	}
}