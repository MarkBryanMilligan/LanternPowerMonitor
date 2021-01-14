package com.lanternsoftware.datamodel.zwave;

import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;

import java.util.List;

@DBSerializable(autogen = false)
public class ZWaveConfig {
	@PrimaryKey
	private int accountId;
	private List<Switch> switches;

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public List<Switch> getSwitches() {
		return switches;
	}

	public void setSwitches(List<Switch> _switches) {
		switches = _switches;
	}
}
