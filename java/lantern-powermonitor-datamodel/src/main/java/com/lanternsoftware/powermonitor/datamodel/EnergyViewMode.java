package com.lanternsoftware.powermonitor.datamodel;


import com.lanternsoftware.util.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public enum EnergyViewMode {
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
		if (this == DAY)
			return DateUtils.addMinutes(_dt, 1);
		if (this == MONTH)
			return DateUtils.addDays(_dt, 1, _tz);
		if (this == YEAR)
			return DateUtils.addMonths(_dt, 1, _tz);
		return _dt;
	}

	public Date decrementBlock(Date _dt, TimeZone _tz) {
		if (this == DAY)
			return DateUtils.addMinutes(_dt, -1);
		if (this == MONTH)
			return DateUtils.addDays(_dt, -1, _tz);
		if (this == YEAR)
			return DateUtils.addMonths(_dt, -1, _tz);
		return _dt;
	}

	public Date incrementView(Date _dt, TimeZone _tz) {
		if (this == DAY)
			return DateUtils.addDays(_dt, 1, _tz);
		if (this == MONTH)
			return DateUtils.addMonths(_dt, 1, _tz);
		if (this == YEAR)
			return DateUtils.addYears(_dt, 1, _tz);
		return _dt;
	}

	public Date decrementView(Date _dt, TimeZone _tz) {
		if (this == DAY)
			return DateUtils.addDays(_dt, -1, _tz);
		if (this == MONTH)
			return DateUtils.addMonths(_dt, -1, _tz);
		if (this == YEAR)
			return DateUtils.addYears(_dt, -1, _tz);
		return _dt;
	}

	public int blockCount(Date _start, TimeZone _tz) {
		if (this == YEAR)
			return 12;
		if (this == MONTH) {
			Calendar end = DateUtils.getEndOfMonthCal(_start, _tz);
			end.add(Calendar.MINUTE, -2);
			return end.get(Calendar.DAY_OF_MONTH);
		}
		if (this == DAY) {
			Date end = DateUtils.getMidnightAfter(_start, _tz);
			return (int)((end.getTime() - _start.getTime()) / 60000);
		}
		return 1;
	}

	public int initBlockCount() {
		if (this == ALL)
			return 1;
		if (this == YEAR)
			return 12;
		if (this == MONTH)
			return 31;
		return 1500;
	}

	public int blockIndex(Date _dayStart, Date _readTime, TimeZone _tz) {
		if (this == DAY) {
			return (int)((_readTime.getTime() - _dayStart.getTime())/60000);
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
