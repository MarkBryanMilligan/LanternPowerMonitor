package com.lanternsoftware.rules.actions;

import com.lanternsoftware.datamodel.rules.Action;
import com.lanternsoftware.datamodel.rules.ActionType;
import com.lanternsoftware.datamodel.rules.Event;
import com.lanternsoftware.datamodel.rules.Rule;

import java.util.List;

public interface ActionImpl {
	ActionType getType();
	void invoke(Rule _rule, List<Event> _event, Action _action);
}
