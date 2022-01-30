package com.lanternsoftware.datamodel.currentmonitor.archive;

import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@DBSerializable(autogen = false)
public class MonthlyEnergyArchive {
	private int accountId;
	private Date month;
	private List<DailyEnergyArchive> days;

	public String getId() {
		return toId(accountId, month);
	}

	public static String toId(int _accountId, Date _month) {
		return String.format("%d-%d", _accountId, DateUtils.toLong(_month));
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public Date getMonth() {
		return month;
	}

	public void setMonth(Date _month) {
		month = _month;
	}

	public List<DailyEnergyArchive> getDays() {
		return days;
	}

	public void setDays(List<DailyEnergyArchive> _days) {
		days = _days;
	}

	public boolean isComplete(TimeZone _tz) {
		Date valid = DateUtils.addDays(new Date(), -7, _tz);
		valid = DateUtils.addMonths(valid, -1, _tz);
		return month.before(valid);
	}
}
