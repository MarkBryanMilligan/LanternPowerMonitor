package com.lanternsoftware.util.dao.mongo.dao;

import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import java.lang.String;
import java.util.Collections;
import java.util.List;

public class MongoConfigSerializer extends AbstractDaoSerializer<MongoConfig>
{
	@Override
	public Class<MongoConfig> getSupportedClass()
	{
		return MongoConfig.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(MongoConfig _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("hosts", _o.getHosts());
		d.put("username", _o.getUsername());
		d.put("password", _o.getPassword());
		d.put("client_keystore_path", _o.getClientKeystorePath());
		d.put("client_keystore_password", _o.getClientKeystorePassword());
		d.put("ca_keystore_path", _o.getCaKeystorePath());
		d.put("ca_keystore_password", _o.getCaKeystorePassword());
		d.put("database_name", _o.getDatabaseName());
		d.put("authentication_database", _o.getAuthenticationDatabase());
		return d;
	}

	@Override
	public MongoConfig fromDaoEntity(DaoEntity _d)
	{
		MongoConfig o = new MongoConfig();
		o.setHosts(DaoSerializer.getList(_d, "hosts", String.class));
		o.setUsername(DaoSerializer.getString(_d, "username"));
		o.setPassword(DaoSerializer.getString(_d, "password"));
		o.setClientKeystorePath(DaoSerializer.getString(_d, "client_keystore_path"));
		o.setClientKeystorePassword(DaoSerializer.getString(_d, "client_keystore_password"));
		o.setCaKeystorePath(DaoSerializer.getString(_d, "ca_keystore_path"));
		o.setCaKeystorePassword(DaoSerializer.getString(_d, "ca_keystore_password"));
		o.setDatabaseName(DaoSerializer.getString(_d, "database_name"));
		o.setAuthenticationDatabase(DaoSerializer.getString(_d, "authentication_database"));
		return o;
	}
}