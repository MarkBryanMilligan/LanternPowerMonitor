package com.lanternsoftware.zwave.dao;

import com.lanternsoftware.datamodel.zwave.ZWaveConfig;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.util.dao.mongo.MongoProxy;

public class MongoZWaveDao implements ZWaveDao {
	private MongoProxy proxy;

	public MongoZWaveDao(MongoConfig _config) {
		proxy = new MongoProxy(_config);
	}

	@Override
	public void shutdown() {
		proxy.shutdown();
	}


	@Override
	public void putConfig(ZWaveConfig _config) {
		proxy.save(_config);
	}

	@Override
	public ZWaveConfig getConfig(int _accountId) {
		return proxy.queryOne(ZWaveConfig.class, new DaoQuery("_id", String.valueOf(_accountId)));
	}
}
