package com.lanternsoftware.powermonitor.datamodel.dao;

import com.lanternsoftware.powermonitor.datamodel.BillingCurrency;
import com.lanternsoftware.powermonitor.datamodel.BillingRate;
import com.lanternsoftware.powermonitor.datamodel.GridFlow;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class BillingRateSerializer extends AbstractDaoSerializer<BillingRate>
{
	@Override
	public Class<BillingRate> getSupportedClass()
	{
		return BillingRate.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(BillingRate _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("meter", _o.getMeter());
		d.put("flow", DaoSerializer.toEnumName(_o.getFlow()));
		d.put("rate", _o.getRate());
		d.put("currency", DaoSerializer.toEnumName(_o.getCurrency()));
		d.put("time_of_day_start", _o.getTimeOfDayStart());
		d.put("time_of_day_end", _o.getTimeOfDayEnd());
		d.put("month_kwh_start", _o.getMonthKWhStart());
		d.put("month_kwh_end", _o.getMonthKWhEnd());
		d.put("begin_effective", DaoSerializer.toLong(_o.getBeginEffective()));
		d.put("end_effective", DaoSerializer.toLong(_o.getEndEffective()));
		d.put("days_of_week", _o.getDaysOfWeek());
		d.put("recurs_annually", _o.isRecursAnnually());
		return d;
	}

	@Override
	public BillingRate fromDaoEntity(DaoEntity _d)
	{
		BillingRate o = new BillingRate();
		o.setMeter(DaoSerializer.getInteger(_d, "meter"));
		o.setFlow(DaoSerializer.getEnum(_d, "flow", GridFlow.class));
		o.setRate(DaoSerializer.getDouble(_d, "rate"));
		o.setCurrency(DaoSerializer.getEnum(_d, "currency", BillingCurrency.class));
		o.setTimeOfDayStart(DaoSerializer.getInteger(_d, "time_of_day_start"));
		o.setTimeOfDayEnd(DaoSerializer.getInteger(_d, "time_of_day_end"));
		o.setMonthKWhStart(DaoSerializer.getDouble(_d, "month_kwh_start"));
		o.setMonthKWhEnd(DaoSerializer.getDouble(_d, "month_kwh_end"));
		o.setBeginEffective(DaoSerializer.getDate(_d, "begin_effective"));
		o.setEndEffective(DaoSerializer.getDate(_d, "end_effective"));
		o.setDaysOfWeek(DaoSerializer.getList(_d, "days_of_week", Integer.class));
		o.setRecursAnnually(DaoSerializer.getBoolean(_d, "recurs_annually"));
		return o;
	}
}