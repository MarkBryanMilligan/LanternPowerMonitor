package com.lanternsoftware.powermonitor.datamodel;


import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Date;

@DBSerializable(autogen = false)
public class BreakerPower {
	private int accountId;
	private int panel;
	private int space;
	private Date readTime;
	private String hubVersion;
	private double power;
	private double voltage;

	public BreakerPower() {
	}

	public BreakerPower(int _panel, int _space, Date _readTime, double _power, double _voltage) {
		panel = _panel;
		space = _space;
		readTime = _readTime;
		power = _power;
		voltage = _voltage;
	}

	public String getId() {
		return String.format("%d-%d-%d", accountId, panel, space);
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public int getPanel() {
		return panel;
	}

	public void setPanel(int _panel) {
		panel = _panel;
	}

	public int getSpace() {
		return space;
	}

	public void setSpace(int _space) {
		space = _space;
	}

	public Date getReadTime() {
		return readTime;
	}

	public void setReadTime(Date _readTime) {
		readTime = _readTime;
	}

	public String getHubVersion() {
		return hubVersion;
	}

	public void setHubVersion(String _hubVersion) {
		hubVersion = _hubVersion;
	}

	public double getPower() {
		return power;
	}

	public void setPower(double _power) {
		power = _power;
	}

	public double getVoltage() {
		return voltage;
	}

	public void setVoltage(double _voltage) {
		voltage = _voltage;
	}

	public String getKey() {
		return Breaker.key(panel, space);
	}
}
