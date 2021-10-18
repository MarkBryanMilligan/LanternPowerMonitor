package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.BillingPlan;
import com.lanternsoftware.datamodel.currentmonitor.BillingRate;
import com.lanternsoftware.datamodel.currentmonitor.BreakerConfig;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroup;
import com.lanternsoftware.datamodel.currentmonitor.BreakerHub;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPanel;
import com.lanternsoftware.datamodel.currentmonitor.Meter;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;

import java.util.Collections;
import java.util.List;

public class BreakerConfigSerializer extends AbstractDaoSerializer<BreakerConfig>
{
	@Override
	public Class<BreakerConfig> getSupportedClass()
	{
		return BreakerConfig.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(BreakerConfig _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("_id", String.valueOf(_o.getAccountId()));
		d.put("meters", DaoSerializer.toDaoEntities(_o.getMeters(), DaoProxyType.MONGO));
		d.put("panels", DaoSerializer.toDaoEntities(_o.getPanels(), DaoProxyType.MONGO));
		d.put("breaker_hubs", DaoSerializer.toDaoEntities(_o.getBreakerHubs(), DaoProxyType.MONGO));
		d.put("breaker_groups", DaoSerializer.toDaoEntities(_o.getBreakerGroups(), DaoProxyType.MONGO));
		d.put("billing_plans", DaoSerializer.toDaoEntities(_o.getBillingPlans(), DaoProxyType.MONGO));
		d.put("billing_rates", DaoSerializer.toDaoEntities(_o.getBillingRates(), DaoProxyType.MONGO));
		d.put("version", _o.getVersion());
		return d;
	}

	@Override
	public BreakerConfig fromDaoEntity(DaoEntity _d)
	{
		BreakerConfig o = new BreakerConfig();
		o.setAccountId(DaoSerializer.getInteger(_d, "_id"));
		o.setMeters(DaoSerializer.getList(_d, "meters", Meter.class));
		o.setPanels(DaoSerializer.getList(_d, "panels", BreakerPanel.class));
		o.setBreakerHubs(DaoSerializer.getList(_d, "breaker_hubs", BreakerHub.class));
		o.setBreakerGroups(DaoSerializer.getList(_d, "breaker_groups", BreakerGroup.class));
		o.setBillingPlans(DaoSerializer.getList(_d, "billing_plans", BillingPlan.class));
		o.setBillingRates(DaoSerializer.getList(_d, "billing_rates", BillingRate.class));
		o.setVersion(DaoSerializer.getInteger(_d, "version"));
		return o;
	}
}