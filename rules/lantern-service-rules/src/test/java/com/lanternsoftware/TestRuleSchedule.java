package com.lanternsoftware;

import com.lanternsoftware.datamodel.rules.EventType;
import com.lanternsoftware.datamodel.rules.Rule;
import com.lanternsoftware.rules.RulesEngine;
import com.lanternsoftware.util.CollectionUtils;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class TestRuleSchedule {
	public static void main(String[] args) {
		TimeZone tz = TimeZone.getTimeZone("America/Chicago"); //TODO: Get from the current monitor account
		List<Rule> rules = CollectionUtils.filter(RulesEngine.instance().dao().getRulesForAccount(100), _r->CollectionUtils.anyQualify(_r.getAllCriteria(), _c->_c.getType() == EventType.TIME));
		if (rules.isEmpty())
			return;
		Collection<Date> dates = CollectionUtils.aggregate(rules, _r->CollectionUtils.transform(_r.getAllCriteria(), _c->_c.getNextTriggerDate(tz), true));
		Date nextDate = CollectionUtils.getSmallest(dates);

	}
}
