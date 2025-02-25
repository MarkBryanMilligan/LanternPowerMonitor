package com.lanternsoftware.powermonitor.datamodel;

import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

@DBSerializable
public class BillingRate {
	private int meter;
	private GridFlow flow;
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

	public GridFlow getFlow() {
		return flow;
	}

	public void setFlow(GridFlow _flow) {
		flow = _flow;
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

	public boolean isApplicable(GridFlow _mode, int _meter, double _monthKWh, int _secondsIntoDay) {
		if ((flow != GridFlow.BOTH) && (flow != _mode))
			return false;
		if ((meter != -1) && (_meter != meter))
			return false;
		if ((monthKWhStart > 0) && (_monthKWh < monthKWhStart))
			return false;
		if ((monthKWhEnd > 0) && (_monthKWh >= monthKWhEnd))
			return false;
		if ((timeOfDayStart == 0) && (timeOfDayEnd == 0))
			return true;
		if ((timeOfDayStart > 0) && (_secondsIntoDay < timeOfDayStart))
			return false;
		return (timeOfDayEnd == 0) || (_secondsIntoDay < timeOfDayEnd);
	}

	public boolean isApplicableForDay(Date _time, TimeZone _tz) {
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
		return true;
	}

	public double apply(double _kWh) {
		return rate * _kWh;
	}

	@Override
	public boolean equals(Object _o) {
		if (this == _o) return true;
		if (_o == null || getClass() != _o.getClass()) return false;
		BillingRate that = (BillingRate) _o;
		return meter == that.meter && Double.compare(that.rate, rate) == 0 && timeOfDayStart == that.timeOfDayStart && timeOfDayEnd == that.timeOfDayEnd && Double.compare(that.monthKWhStart, monthKWhStart) == 0 && Double.compare(that.monthKWhEnd, monthKWhEnd) == 0 && recursAnnually == that.recursAnnually && flow == that.flow && currency == that.currency && Objects.equals(beginEffective, that.beginEffective) && Objects.equals(endEffective, that.endEffective);
	}

	@Override
	public int hashCode() {
		return Objects.hash(meter, flow, rate, currency, timeOfDayStart, timeOfDayEnd, monthKWhStart, monthKWhEnd, beginEffective, endEffective, recursAnnually);
	}

	public BillingRate duplicate() {
		BillingRate r = new BillingRate();
		r.setMeter(meter);
		r.setFlow(flow);
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
