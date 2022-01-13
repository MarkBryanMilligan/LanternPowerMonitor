package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.HubCommand;
import com.lanternsoftware.datamodel.currentmonitor.HubCommands;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class HubCommandsSerializer extends AbstractDaoSerializer<HubCommands>
{
	@Override
	public Class<HubCommands> getSupportedClass()
	{
		return HubCommands.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(HubCommands _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("commands", DaoSerializer.toDaoEntities(_o.getCommands(), DaoProxyType.MONGO));
		return d;
	}

	@Override
	public HubCommands fromDaoEntity(DaoEntity _d)
	{
		HubCommands o = new HubCommands();
		o.setCommands(DaoSerializer.getList(_d, "commands", HubCommand.class));
		return o;
	}
}