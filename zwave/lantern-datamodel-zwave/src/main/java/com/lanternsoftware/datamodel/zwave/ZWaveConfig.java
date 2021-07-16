package com.lanternsoftware.datamodel.zwave;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;

import java.util.List;

@DBSerializable(autogen = false)
public class ZWaveConfig {
	@PrimaryKey private int accountId;
	private String commPort;
	private String url;
	private String masterUrl;
	private String rulesUrl;
	private List<Switch> switches;

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public String getCommPort() {
		return commPort;
	}

	public void setCommPort(String _commPort) {
		commPort = _commPort;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String _url) {
		url = _url;
	}

	public String getMasterUrl() {
		return masterUrl;
	}

	public void setMasterUrl(String _masterUrl) {
		masterUrl = _masterUrl;
	}

	public String getRulesUrl() {
		return rulesUrl;
	}

	public void setRulesUrl(String _rulesUrl) {
		rulesUrl = _rulesUrl;
	}

	public List<Switch> getSwitches() {
		return switches;
	}

	public void setSwitches(List<Switch> _switches) {
		switches = _switches;
	}

	public boolean isMaster() {
		return NullUtils.isEqual(url, masterUrl);
	}

	public List<Switch> getSwitchesForThisController() {
		return CollectionUtils.filter(switches, this::isMySwitch);
	}

	public boolean isMySwitch(Switch _sw) {
		return (isMaster() && NullUtils.isEmpty(_sw.getControllerUrl())) || _sw.isControlledBy(getUrl());
	}
}
