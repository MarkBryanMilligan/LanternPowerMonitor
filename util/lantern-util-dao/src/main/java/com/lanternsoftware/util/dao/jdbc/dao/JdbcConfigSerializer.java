package com.lanternsoftware.util.dao.jdbc.dao;

import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.jdbc.DatabaseType;
import com.lanternsoftware.util.dao.jdbc.JdbcConfig;
import java.util.Collections;
import java.util.List;

public class JdbcConfigSerializer extends AbstractDaoSerializer<JdbcConfig>
{
	@Override
	public Class<JdbcConfig> getSupportedClass()
	{
		return JdbcConfig.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(JdbcConfig _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("type", DaoSerializer.toEnumName(_o.getType()));
		d.put("username", _o.getUsername());
		d.put("password", _o.getPassword());
		d.put("hostname", _o.getHostname());
		d.put("database", _o.getDatabase());
		d.put("port", _o.getPort());
		return d;
	}

	@Override
	public JdbcConfig fromDaoEntity(DaoEntity _d)
	{
		JdbcConfig o = new JdbcConfig();
		o.setType(DaoSerializer.getEnum(_d, "type", DatabaseType.class));
		o.setUsername(DaoSerializer.getString(_d, "username"));
		o.setPassword(DaoSerializer.getString(_d, "password"));
		o.setHostname(DaoSerializer.getString(_d, "hostname"));
		o.setDatabase(DaoSerializer.getString(_d, "database"));
		o.setPort(DaoSerializer.getString(_d, "port"));
		return o;
	}
}