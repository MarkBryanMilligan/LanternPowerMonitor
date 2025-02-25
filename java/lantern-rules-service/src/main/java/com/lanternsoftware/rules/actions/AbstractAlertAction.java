package com.lanternsoftware.rules.actions;

import com.lanternsoftware.datamodel.rules.Alert;
import com.lanternsoftware.datamodel.rules.Rule;
import com.lanternsoftware.rules.RulesEngine;

public abstract class AbstractAlertAction implements ActionImpl {
	protected void sendAlert(Rule _rule, Alert _alert) {
		RulesEngine.instance().sendFcmMessage(_rule.getAccountId(), _alert);
	}
}
