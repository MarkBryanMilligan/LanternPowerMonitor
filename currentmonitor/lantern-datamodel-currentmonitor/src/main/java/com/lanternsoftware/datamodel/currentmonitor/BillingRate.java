package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@DBSerializable
public class BillingRate {
	private int meter;
	private int dayBillingCycleStart;
	private BillingMode mode;
	private double rate;
	private BillingCurrency currency;
	private int timeOfDayStart;
	private int timeOfDayEnd;
	private double monthKWhStart;
	private double monthKWhEnd;
	private Date beginEffective;
	private Date endEffective;
	boolean recursAnnually;

	public int getMeter() {
		return meter;
	}

	public void setMeter(int _meter) {
		meter = _meter;
	}

	public int getDayBillingCycleStart() {
		return dayBillingCycleStart;
	}

	public void setDayBillingCycleStart(int _dayBillingCycleStart) {
		dayBillingCycleStart = _dayBillingCycleStart;
	}

	public BillingMode getMode() {
		return mode;
	}

	public void setMode(BillingMode _mode) {
		mode = _mode;
	}

	public double getRate() {
		return rate;
	}

	public void setRate(double _rate) {
		rate = _rate;
	}

	public BillingCurrency getCurrency() {
		return currency;
	}

	public void setCurrency(BillingCurrency _currency) {
		currency = _currency;
	}

	public int getTimeOfDayStart() {
		return timeOfDayStart;
	}

	public void setTimeOfDayStart(int _timeOfDayStart) {
		timeOfDayStart = _timeOfDayStart;
	}

	public int getTimeOfDayEnd() {
		return timeOfDayEnd;
	}

	public void setTimeOfDayEnd(int _timeOfDayEnd) {
		timeOfDayEnd = _timeOfDayEnd;
	}

	public double getMonthKWhStart() {
		return monthKWhStart;
	}

	public void setMonthKWhStart(double _monthKWhStart) {
		monthKWhStart = _monthKWhStart;
	}

	public double getMonthKWhEnd() {
		return monthKWhEnd;
	}

	public void setMonthKWhEnd(double _monthKWhEnd) {
		monthKWhEnd = _monthKWhEnd;
	}

	public Date getBeginEffective() {
		return beginEffective;
	}

	public void setBeginEffective(Date _beginEffective) {
		beginEffective = _beginEffective;
	}

	public Date getEndEffective() {
		return endEffective;
	}

	public void setEndEffective(Date _endEffective) {
		endEffective = _endEffective;
	}

	public boolean isRecursAnnually() {
		return recursAnnually;
	}

	public void setRecursAnnually(boolean _recursAnnually) {
		recursAnnually = _recursAnnually;
	}

	public boolean isApplicable(BillingMode _mode, int _meter, double _monthKWh, Date _time, TimeZone _tz) {
		if ((mode != BillingMode.ANY_DIRECTION) && (mode != _mode))
			return false;
		if ((meter != -1) && (_meter != meter))
			return false;
		if ((monthKWhStart > 0) && (_monthKWh < monthKWhStart))
			return false;
		if ((monthKWhEnd > 0) && (_monthKWh >= monthKWhEnd))
			return false;
		if ((beginEffective != null) && (endEffective != null) && recursAnnually) {
			Date begin = beginEffective;
			Date end = endEffective;
			while (_time.before(begin)) {
				begin = DateUtils.addYears(begin, -1, _tz);
				end = DateUtils.addYears(end, -1, _tz);
			}
			while (_time.after(end)) {
				begin = DateUtils.addYears(begin, 1, _tz);
				end = DateUtils.addYears(end, 1, _tz);
			}
			if (!DateUtils.isBetween(_time, begin, end))
				return false;
		}
		else {
			if ((beginEffective != null) && _time.before(beginEffective))
				return false;
			if ((endEffective != null) && endEffective.before(_time))
				return false;
		}
		if ((timeOfDayStart == 0) && (timeOfDayEnd == 0))
			return true;
		Calendar midnight = DateUtils.getMidnightBeforeCal(_time, _tz);
		int timeOfDay = (int)((_time.getTime() - midnight.getTimeInMillis()) / 1000);
		if ((timeOfDayStart > 0) && (timeOfDay < timeOfDayStart))
			return false;
		return (timeOfDayEnd == 0) || (timeOfDay < timeOfDayEnd);
	}

	public double apply(double _kWh) {
		return rate * _kWh;
	}

	public BillingRate duplicate() {
		BillingRate r = new BillingRate();
		r.setMeter(meter);
		r.setDayBillingCycleStart(dayBillingCycleStart);
		r.setMode(mode);
		r.setRate(rate);
		r.setCurrency(currency);
		r.setTimeOfDayStart(timeOfDayStart);
		r.setTimeOfDayEnd(timeOfDayEnd);
		r.setMonthKWhStart(monthKWhStart);
		r.setMonthKWhEnd(monthKWhEnd);
		r.setBeginEffective(beginEffective);
		r.setEndEffective(endEffective);
		r.setRecursAnnually(recursAnnually);
		return r;
	}
}
