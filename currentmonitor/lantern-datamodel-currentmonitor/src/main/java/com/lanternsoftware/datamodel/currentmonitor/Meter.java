package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class Meter {
	private int accountId;
	private int index;
	private String name;

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int _index) {
		index = _index;
	}

	public String getName() {
		return name;
	}

	public void setName(String _name) {
		name = _name;
	}
}
