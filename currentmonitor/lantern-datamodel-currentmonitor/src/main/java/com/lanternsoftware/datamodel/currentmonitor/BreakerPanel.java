package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class BreakerPanel {
	private int accountId;
	private String name;
	private int index;
	private int spaces;
	private int meter;

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public String getName() {
		return name;
	}

	public void setName(String _name) {
		name = _name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int _index) {
		index = _index;
	}

	public int getSpaces() {
		return spaces;
	}

	public void setSpaces(int _spaces) {
		spaces = _spaces;
	}

	public int getMeter() {
		return meter;
	}

	public void setMeter(int _meter) {
		meter = _meter;
	}
}
