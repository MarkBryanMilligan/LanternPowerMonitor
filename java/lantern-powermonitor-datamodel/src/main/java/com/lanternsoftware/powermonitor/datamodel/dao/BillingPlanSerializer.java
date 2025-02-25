package com.lanternsoftware.powermonitor.datamodel.dao;

import com.lanternsoftware.powermonitor.datamodel.BillingPlan;
import com.lanternsoftware.powermonitor.datamodel.BillingRate;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class BillingPlanSerializer extends AbstractDaoSerializer<BillingPlan>
{
	@Override
	public Class<BillingPlan> getSupportedClass()
	{
		return BillingPlan.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(BillingPlan _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("account_id", _o.getAccountId());
		d.put("plan_id", _o.getPlanId());
		d.put("billing_day", _o.getBillingDay());
		d.put("name", _o.getName());
		d.put("rates", DaoSerializer.toDaoEntities(_o.getRates(), DaoProxyType.MONGO));
		return d;
	}

	@Override
	public BillingPlan fromDaoEntity(DaoEntity _d)
	{
		BillingPlan o = new BillingPlan();
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setPlanId(DaoSerializer.getInteger(_d, "plan_id"));
		o.setBillingDay(DaoSerializer.getInteger(_d, "billing_day"));
		o.setName(DaoSerializer.getString(_d, "name"));
		o.setRates(DaoSerializer.getList(_d, "rates", BillingRate.class));
		return o;
	}
}