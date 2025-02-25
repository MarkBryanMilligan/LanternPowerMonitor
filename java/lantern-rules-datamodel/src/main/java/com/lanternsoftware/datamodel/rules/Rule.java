package com.lanternsoftware.datamodel.rules;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@DBSerializable
public class Rule {
	@PrimaryKey	private String id;
	private int accountId;
	private boolean or;
	private List<Criteria> criteria;
	private List<Action> actions;

	public String getId() {
		return id;
	}

	public void setId(String _id) {
		id = _id;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public boolean isOr() {
		return or;
	}

	public void setOr(boolean _or) {
		or = _or;
	}

	public List<Criteria> getCriteria() {
		return criteria;
	}

	public List<Criteria> getAllCriteria() {
		List<Criteria> allCriteria = new ArrayList<>();
		CollectionUtils.edit(criteria, _c->_c.addAllCriteria(allCriteria));
		return allCriteria;
	}

	public void setCriteria(List<Criteria> _criteria) {
		criteria = _criteria;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> _actions) {
		actions = _actions;
	}

	public boolean isMet(List<Event> _events, TimeZone _tz) {
		if (or)
			return CollectionUtils.anyQualify(criteria, _c->_c.isMet(_events, _tz));
		return CollectionUtils.allQualify(criteria, _c->_c.isMet(_events, _tz));
	}

	public List<Criteria> getCriteriaNeedingData(Event _event) {
		List<Criteria> allCriteria = getAllCriteria();
		allCriteria.removeIf(_c->_c.triggers(_event));
		return allCriteria;
	}

	public boolean triggers(Event _event) {
		return CollectionUtils.anyQualify(criteria, _c-> _c.triggers(_event));
	}
}
