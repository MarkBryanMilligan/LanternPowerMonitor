package com.lanternsoftware;

import com.lanternsoftware.datamodel.rules.Action;
import com.lanternsoftware.datamodel.rules.ActionType;
import com.lanternsoftware.datamodel.rules.Criteria;
import com.lanternsoftware.datamodel.rules.EventType;
import com.lanternsoftware.datamodel.rules.Operator;
import com.lanternsoftware.datamodel.rules.Rule;
import com.lanternsoftware.rules.RulesEngine;
import com.lanternsoftware.util.CollectionUtils;

public class CreateRules {
	public static void main(String[] args) {
		for (Rule r : RulesEngine.instance().dao().getRulesForAccount(100)) {
			RulesEngine.instance().dao().deleteRule(r.getId());
		}
		Rule r1 = new Rule();
		r1.setAccountId(100);
		Criteria c1 = new Criteria();
		c1.setType(EventType.SWITCH_LEVEL);
		c1.setSourceId("203");
		c1.setOperator(Operator.EQUAL);
		c1.setValue(1);
		r1.setCriteria(CollectionUtils.asArrayList(c1));
		Action a1 = new Action();
		a1.setType(ActionType.MOBILE_ALERT_STATIC);
		a1.setDescription("Garage Door 1 opened");
		a1.setDestinationId("*");
		r1.setActions(CollectionUtils.asArrayList(a1));
		RulesEngine.instance().dao().putRule(r1);

		Rule r2 = new Rule();
		r2.setAccountId(100);
		Criteria c2 = new Criteria();
		c2.setType(EventType.SWITCH_LEVEL);
		c2.setSourceId("203");
		c2.setOperator(Operator.EQUAL);
		c2.setValue(0);
		r2.setCriteria(CollectionUtils.asArrayList(c2));
		Action a2 = new Action();
		a2.setType(ActionType.MOBILE_ALERT_STATIC);
		a2.setDescription("Garage Door 1 closed");
		a2.setDestinationId("*");
		r2.setActions(CollectionUtils.asArrayList(a2));
		RulesEngine.instance().dao().putRule(r2);

		Rule r3 = new Rule();
		r3.setAccountId(100);
		Criteria c3 = new Criteria();
		c3.setType(EventType.SWITCH_LEVEL);
		c3.setSourceId("204");
		c3.setOperator(Operator.EQUAL);
		c3.setValue(1);
		r3.setCriteria(CollectionUtils.asArrayList(c3));
		Action a3 = new Action();
		a3.setType(ActionType.MOBILE_ALERT_STATIC);
		a3.setDescription("Garage Door 2 opened");
		a3.setDestinationId("*");
		r3.setActions(CollectionUtils.asArrayList(a3));
		RulesEngine.instance().dao().putRule(r3);

		Rule r4 = new Rule();
		r4.setAccountId(100);
		Criteria c4 = new Criteria();
		c4.setType(EventType.SWITCH_LEVEL);
		c4.setSourceId("204");
		c4.setOperator(Operator.EQUAL);
		c4.setValue(0);
		r4.setCriteria(CollectionUtils.asArrayList(c4));
		Action a4 = new Action();
		a4.setType(ActionType.MOBILE_ALERT_STATIC);
		a4.setDescription("Garage Door 2 closed");
		a4.setDestinationId("*");
		r4.setActions(CollectionUtils.asArrayList(a4));
		RulesEngine.instance().dao().putRule(r4);

		Rule r5 = new Rule();
		r5.setAccountId(100);
		Criteria c5 = new Criteria();
		c5.setType(EventType.SWITCH_LEVEL);
		c5.setSourceId("205");
		c5.setOperator(Operator.EQUAL);
		c5.setValue(1);
		r5.setCriteria(CollectionUtils.asArrayList(c5));
		Action a5 = new Action();
		a5.setType(ActionType.MOBILE_ALERT_STATIC);
		a5.setDescription("Garage Door 3 opened");
		a5.setDestinationId("*");
		r5.setActions(CollectionUtils.asArrayList(a5));
		RulesEngine.instance().dao().putRule(r5);

		Rule r6 = new Rule();
		r6.setAccountId(100);
		Criteria c6 = new Criteria();
		c6.setType(EventType.SWITCH_LEVEL);
		c6.setSourceId("205");
		c6.setOperator(Operator.EQUAL);
		c6.setValue(0);
		r6.setCriteria(CollectionUtils.asArrayList(c6));
		Action a6 = new Action();
		a6.setType(ActionType.MOBILE_ALERT_STATIC);
		a6.setDescription("Garage Door 3 closed");
		a6.setDestinationId("*");
		r6.setActions(CollectionUtils.asArrayList(a6));
		RulesEngine.instance().dao().putRule(r6);

		Rule r7 = new Rule();
		r7.setAccountId(100);
		Criteria c7 = new Criteria();
		c7.setType(EventType.SWITCH_LEVEL);
		c7.setSourceId("203");
		c7.setOperator(Operator.EQUAL);
		c7.setValue(1);
		Criteria c7_2 = new Criteria();
		c7_2.setType(EventType.TIME);
		c7_2.setValue(79200);
//		c7_2.setValue(74400);
		r7.setCriteria(CollectionUtils.asArrayList(c7, c7_2));
		Action a7 = new Action();
		a7.setType(ActionType.MOBILE_ALERT_STATIC);
		a7.setDescription("Garage Door 1 is still open");
		a7.setDestinationId("*");
		r7.setActions(CollectionUtils.asArrayList(a7));
		RulesEngine.instance().dao().putRule(r7);

		Rule r8 = new Rule();
		r8.setAccountId(100);
		Criteria c8 = new Criteria();
		c8.setType(EventType.SWITCH_LEVEL);
		c8.setSourceId("204");
		c8.setOperator(Operator.EQUAL);
		c8.setValue(1);
		Criteria c8_2 = new Criteria();
		c8_2.setType(EventType.TIME);
		c8_2.setValue(79200);
//		c8_2.setValue(74400);
		r8.setCriteria(CollectionUtils.asArrayList(c8, c8_2));
		Action a8 = new Action();
		a8.setType(ActionType.MOBILE_ALERT_STATIC);
		a8.setDescription("Garage Door 2 is still open");
		a8.setDestinationId("*");
		r8.setActions(CollectionUtils.asArrayList(a8));
		RulesEngine.instance().dao().putRule(r8);

		Rule r9 = new Rule();
		r9.setAccountId(100);
		Criteria c9 = new Criteria();
		c9.setType(EventType.SWITCH_LEVEL);
		c9.setSourceId("205");
		c9.setOperator(Operator.EQUAL);
		c9.setValue(1);
		Criteria c9_2 = new Criteria();
		c9_2.setType(EventType.TIME);
		c9_2.setValue(79200);
//		c9_2.setValue(74400);
		r9.setCriteria(CollectionUtils.asArrayList(c9, c9_2));
		Action a9 = new Action();
		a9.setType(ActionType.MOBILE_ALERT_STATIC);
		a9.setDescription("Garage Door 3 is still open");
		a9.setDestinationId("*");
		r9.setActions(CollectionUtils.asArrayList(a9));
		RulesEngine.instance().dao().putRule(r9);
		RulesEngine.shutdown();
	}
}
