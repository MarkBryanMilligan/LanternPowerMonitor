package com.lanternsoftware.rules.actions;

import com.lanternsoftware.datamodel.rules.Action;
import com.lanternsoftware.datamodel.rules.ActionType;
import com.lanternsoftware.datamodel.rules.Alert;
import com.lanternsoftware.datamodel.rules.Event;
import com.lanternsoftware.datamodel.rules.Rule;
import com.lanternsoftware.util.CollectionUtils;

import java.util.List;

public class MobileAlertStatic extends AbstractAlertAction {
	@Override
	public ActionType getType() {
		return ActionType.MOBILE_ALERT_STATIC;
	}

	@Override
	public void invoke(Rule _rule, List<Event> _event, Action _action) {
		sendAlert(_rule, new Alert(_action.getDescription()));
	}
}
