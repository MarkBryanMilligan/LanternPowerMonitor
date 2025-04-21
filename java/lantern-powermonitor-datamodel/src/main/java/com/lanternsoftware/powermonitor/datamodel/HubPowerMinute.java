package com.lanternsoftware.powermonitor.datamodel;

import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Date;
import java.util.List;

@DBSerializable(autogen = false)
public class HubPowerMinute {
	private int accountId;
	private int hub;
	private int minute;
	private float voltage;
	private List<BreakerPowerMinute> breakers;

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public int getHub() {
		return hub;
	}

	public void setHub(int _hub) {
		hub = _hub;
	}

	public Date getMinuteAsDate() {
		return new Date(((long)minute)*60000);
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int _minute) {
		minute = _minute;
	}

	public void setMinute(Date _minute) {
		minute = (int)(_minute.getTime()/60000);
	}

	public float getVoltage() {
		return voltage;
	}

	public void setVoltage(float _voltage) {
		voltage = _voltage;
	}

	public List<BreakerPowerMinute> getBreakers() {
		return breakers;
	}

	public void setBreakers(List<BreakerPowerMinute> _breakers) {
		breakers = _breakers;
	}

	public String getId() {
		return String.format("%d-%d-%d", accountId, hub, minute);
	}
}
