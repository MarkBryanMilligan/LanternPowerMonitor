package com.lanternsoftware.dataaccess.rules;

import com.lanternsoftware.datamodel.rules.Event;
import com.lanternsoftware.datamodel.rules.EventType;
import com.lanternsoftware.datamodel.rules.FcmDevice;
import com.lanternsoftware.datamodel.rules.Rule;

import java.util.Date;
import java.util.List;

public interface RulesDataAccess {
	void shutdown();
	void putRule(Rule _rule);
	List<Rule> getRulesForAccount(int _accountId);
	void deleteRule(String _ruleId);
	void putEvent(Event _event);
	Event getMostRecentEvent(int _accountId, EventType _type, String _sourceId);
	List<Event> getEvents(int _accountId, EventType _type, String _sourceId, Date _from, Date _to);
	void putFcmDevice(FcmDevice _device);
	void removeFcmDevice(String _id);
	List<FcmDevice> getFcmDevicesForAccount(int _accountId);
}
