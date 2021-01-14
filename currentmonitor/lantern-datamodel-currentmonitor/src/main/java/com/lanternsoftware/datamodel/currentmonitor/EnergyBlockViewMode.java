package com.lanternsoftware.datamodel.currentmonitor;


import com.lanternsoftware.util.DateUtils;

import javax.management.remote.rmi._RMIConnection_Stub;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public enum EnergyBlockViewMode {
	DAY,
	MONTH,
	YEAR,
	ALL;

	public Date toStart(Date _dt, TimeZone _tz) {
		if (this == DAY)
			return DateUtils.getMidnightBefore(_dt, _tz);
		if (this == MONTH)
			return DateUtils.getStartOfMonth(_dt, _tz);
		if (this == YEAR)
			return DateUtils.getStartOfYear(_dt, _tz);
		return new Date(0);
	}

	public Date toEnd(Date _dt, TimeZone _tz) {
		if (this == DAY)
			return DateUtils.getMidnightAfter(_dt, _tz);
		if (this == MONTH)
			return DateUtils.getEndOfMonth(_dt, _tz);
		if (this == YEAR)
			return DateUtils.getEndOfYear(_dt, _tz);
		return new Date(0);
	}

	public Date toBlockStart(Date _dt, TimeZone _tz) {
		if (this == DAY)
			return DateUtils.getStartOfMinute(_dt, _tz);
		if (this == MONTH)
			return DateUtils.getMidnightBefore(_dt, _tz);
		if (this == YEAR)
			return DateUtils.getStartOfMonth(_dt, _tz);
		return new Date(0);
	}

	public Date toBlockEnd(Date _dt, TimeZone _tz) {
		if (this == DAY)
			return DateUtils.getEndOfMinute(_dt, _tz);
		if (this == MONTH)
			return DateUtils.getMidnightAfter(_dt, _tz);
		if (this == YEAR)
			return DateUtils.getEndOfMonth(_dt, _tz);
		return new Date(0);
	}

	public Date incrementBlock(Date _dt, TimeZone _tz) {
		Calendar cal = DateUtils.toCalendar(_dt, _tz);
		if (this == DAY)
			cal.add(Calendar.MINUTE, 1);
		else if (this == MONTH)
			cal.add(Calendar.DAY_OF_YEAR, 1);
		if (this == YEAR)
			cal.add(Calendar.MONTH, 1);
		return cal.getTime();
	}

	public Date decrementBlock(Date _dt, TimeZone _tz) {
		Calendar cal = DateUtils.toCalendar(_dt, _tz);
		if (this == DAY)
			cal.add(Calendar.MINUTE, -1);
		else if (this == MONTH)
			cal.add(Calendar.DAY_OF_YEAR, -1);
		if (this == YEAR)
			cal.add(Calendar.MONTH, -1);
		return cal.getTime();
	}

	public Date incrementView(Date _dt, TimeZone _tz) {
		Calendar cal = DateUtils.toCalendar(_dt, _tz);
		if (this == DAY)
			cal.add(Calendar.DAY_OF_YEAR, 1);
		else if (this == MONTH)
			cal.add(Calendar.MONTH, 1);
		if (this == YEAR)
			cal.add(Calendar.YEAR, 1);
		return cal.getTime();
	}

	public Date decrementView(Date _dt, TimeZone _tz) {
		Calendar cal = DateUtils.toCalendar(_dt, _tz);
		if (this == DAY)
			cal.add(Calendar.DAY_OF_YEAR, -1);
		else if (this == MONTH)
			cal.add(Calendar.MONTH, -1);
		if (this == YEAR)
			cal.add(Calendar.YEAR, -1);
		return cal.getTime();
	}

	public int blockCount(Date _start, TimeZone _tz) {
		if (this == ALL)
			return 1;
		Date end = toEnd(_start, _tz);
		int blockCnt = 0;
		while (_start.before(end)) {
			blockCnt++;
			_start = toBlockEnd(_start, _tz);
		}
		return blockCnt;
	}

	public int blockIndex(Date _readTime, TimeZone _tz) {
		if (this == DAY) {
			Date start = DateUtils.getMidnightBefore(_readTime, _tz);
			return (int)((_readTime.getTime() - start.getTime())/60000);
		}
		else if (this == MONTH) {
			Calendar read = DateUtils.toCalendar(_readTime, _tz);
			return read.get(Calendar.DAY_OF_MONTH) - 1;
		}
		if (this == YEAR) {
			Calendar read = DateUtils.toCalendar(_readTime, _tz);
			return read.get(Calendar.MONTH);
		}
		return 0;
	}

	public Date toBlockStart(int _index, Date _start, TimeZone _tz) {
		if (this == DAY)
			return new Date(_start.getTime() + _index*60000);
		else if (this == MONTH) {
			Calendar read = DateUtils.toCalendar(_start, _tz);
			read.add(Calendar.DAY_OF_MONTH, _index);
			return read.getTime();
		}
		if (this == YEAR) {
			Calendar read = DateUtils.toCalendar(_start, _tz);
			read.add(Calendar.MONTH, _index);
			return read.getTime();
		}
		return new Date(0);
	}
}
