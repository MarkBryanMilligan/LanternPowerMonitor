package com.lanternsoftware.powermonitor.dataaccess;

import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Date;

@DBSerializable(autogen = false)
public class DirtyMinute {
	private int accountId;
	private int minute;
	private Date posted;

	public DirtyMinute() {
	}

	public DirtyMinute(int _accountId, int _minute, Date _posted) {
		accountId = _accountId;
		minute = _minute;
		posted = _posted;
	}

	public String getId() {
		return accountId + "-" + minute;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
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

	public Date getPosted() {
		return posted;
	}

	public void setPosted(Date _posted) {
		posted = _posted;
	}
}
