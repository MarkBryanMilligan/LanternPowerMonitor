package com.lanternsoftware;

import com.lanternsoftware.datamodel.rules.Event;
import com.lanternsoftware.datamodel.rules.EventType;
import com.lanternsoftware.rules.RulesEngine;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.concurrency.ConcurrencyUtils;

import java.util.TimeZone;

public class TestTimeRule {
	public static void main(String[] args) {
		Event event = new Event();
		event.setAccountId(100);
		event.setTime(DateUtils.date(7, 15, 2021, 18, 30, 0, 0, TimeZone.getTimeZone("America/Chicago")));
		event.setType(EventType.TIME);
		RulesEngine.instance().fireEvent(event);
		ConcurrencyUtils.sleep(200000);
		RulesEngine.shutdown();
	}
}
