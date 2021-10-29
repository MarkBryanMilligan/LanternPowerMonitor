package com.lanternsoftware.thermometer.config.dao;

import com.lanternsoftware.thermometer.config.EnvironmentConfig;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class EnvironmentConfigSerializer extends AbstractDaoSerializer<EnvironmentConfig>
{
	@Override
	public Class<EnvironmentConfig> getSupportedClass()
	{
		return EnvironmentConfig.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(EnvironmentConfig _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("co2serial_port", _o.getCo2serialPort());
		return d;
	}

	@Override
	public EnvironmentConfig fromDaoEntity(DaoEntity _d)
	{
		EnvironmentConfig o = new EnvironmentConfig();
		o.setCo2serialPort(DaoSerializer.getString(_d, "co2serial_port"));
		return o;
	}
}