package com.lanternsoftware.dataaccess.rules;

import com.lanternsoftware.datamodel.rules.Event;
import com.lanternsoftware.datamodel.rules.EventType;
import com.lanternsoftware.datamodel.rules.FcmDevice;
import com.lanternsoftware.datamodel.rules.Rule;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.DaoSort;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.util.dao.mongo.MongoProxy;

import java.util.Date;
import java.util.List;

public class MongoRulesDataAccess implements RulesDataAccess {
	private final MongoProxy proxy;

	public MongoRulesDataAccess(MongoConfig _config) {
		proxy = new MongoProxy(_config);
		proxy.ensureIndex(Rule.class, DaoSort.sort("account_id"));
		proxy.ensureIndex(Event.class, DaoSort.sort("account_id").then("type").then("source_id").then("time"));
		proxy.ensureIndex(FcmDevice.class, DaoSort.sort("account_id"));
	}

	@Override
	public void shutdown() {
		proxy.shutdown();
	}

	@Override
	public void putRule(Rule _rule) {
		proxy.save(_rule);
	}

	@Override
	public List<Rule> getRulesForAccount(int _accountId) {
		return proxy.query(Rule.class, new DaoQuery("account_id", _accountId));
	}

	@Override
	public void deleteRule(String _ruleId) {
		proxy.delete(Rule.class, new DaoQuery("_id", _ruleId));
	}

	@Override
	public void putEvent(Event _event) {
		proxy.save(_event);
	}

	@Override
	public Event getMostRecentEvent(int _accountId, EventType _type, String _sourceId) {
		return proxy.queryOne(Event.class, new DaoQuery("account_id", _accountId).and("type", DaoSerializer.toEnumName(_type)).and("source_id", _sourceId), DaoSort.sortDesc("time"));
	}

	@Override
	public List<Event> getEvents(int _accountId, EventType _type, String _sourceId, Date _from, Date _to) {
		return proxy.query(Event.class, new DaoQuery("account_id", _accountId).and("type", DaoSerializer.toEnumName(_type)).and("source_id", _sourceId).andBetweenInclusiveExclusive("time", DaoSerializer.toLong(_from), DaoSerializer.toLong(_to)));
	}

	@Override
	public void putFcmDevice(FcmDevice _device) {
		proxy.delete(FcmDevice.class, new DaoQuery("account_id", _device.getAccountId()).and("token", _device.getToken()));
		proxy.save(_device);
	}

	@Override
	public List<FcmDevice> getFcmDevicesForAccount(int _accountId) {
		return proxy.query(FcmDevice.class, new DaoQuery("account_id", _accountId));
	}
}
