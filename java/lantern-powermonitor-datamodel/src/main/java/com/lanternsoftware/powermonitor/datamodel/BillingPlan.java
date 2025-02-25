package com.lanternsoftware.powermonitor.datamodel;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.IIdentical;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@DBSerializable
public class BillingPlan implements IIdentical<BillingPlan> {
	private int accountId;
	private int planId;
	private int billingDay;
	private String name;
	private List<BillingRate> rates;

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public int getPlanId() {
		return planId;
	}

	public void setPlanId(int _planId) {
		planId = _planId;
	}

	public int getBillingDay() {
		return billingDay;
	}

	public void setBillingDay(int _billingDay) {
		billingDay = _billingDay;
	}

	public Date getBillingCycleStart(Date _for, TimeZone _tz) {
		Calendar cal = DateUtils.getStartOfMonthCal(_for, _tz);
		if (billingDay < 100) {
			cal.set(Calendar.DAY_OF_MONTH, (billingDay == 0) ? 1 : billingDay);
			if (cal.getTime().after(_for))
				cal.add(Calendar.MONTH, -1);
			return cal.getTime();
		}
		int dayOfWeek = (billingDay-101)%7 + 1;
		int weekOfMonth = (billingDay-101)/7;
		int dayOfMonthOffset = dayOfWeek-cal.get(Calendar.DAY_OF_WEEK);
		if (dayOfMonthOffset < 0)
			dayOfMonthOffset += 7;
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonthOffset+(7*weekOfMonth)+1);
		return cal.getTime();
	}

	public Date getBillingCycleEnd(Date _for, TimeZone _tz) {
		return getBillingCycleStart(DateUtils.addMonths(_for, 1, _tz), _tz);
	}

	public String getName() {
		return name;
	}

	public void setName(String _name) {
		name = _name;
	}

	public List<BillingRate> getRates() {
		return rates;
	}

	public void setRates(List<BillingRate> _rates) {
		rates = _rates;
	}

	@Override
	public boolean isIdentical(BillingPlan _other) {
		return accountId == _other.accountId && planId == _other.planId && billingDay == _other.billingDay && CollectionUtils.isEqual(rates, _other.rates);
	}
}
