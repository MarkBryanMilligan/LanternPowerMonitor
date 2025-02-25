package com.lanternsoftware.powermonitor.datamodel.hub.dao;

import com.lanternsoftware.powermonitor.datamodel.hub.BreakerSample;
import com.lanternsoftware.powermonitor.datamodel.hub.PowerSample;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class BreakerSampleSerializer extends AbstractDaoSerializer<BreakerSample>
{
	@Override
	public Class<BreakerSample> getSupportedClass()
	{
		return BreakerSample.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(BreakerSample _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("panel", _o.getPanel());
		d.put("space", _o.getSpace());
		d.put("calculated_voltage", _o.getCalculatedVoltage());
		d.put("calculated_power", _o.getCalculatedPower());
		d.put("log", _o.getLog());
		d.put("samples", DaoSerializer.toDaoEntities(_o.getSamples(), DaoProxyType.MONGO));
		return d;
	}

	@Override
	public BreakerSample fromDaoEntity(DaoEntity _d)
	{
		BreakerSample o = new BreakerSample();
		o.setPanel(DaoSerializer.getInteger(_d, "panel"));
		o.setSpace(DaoSerializer.getInteger(_d, "space"));
		o.setCalculatedVoltage(DaoSerializer.getDouble(_d, "calculated_voltage"));
		o.setCalculatedPower(DaoSerializer.getDouble(_d, "calculated_power"));
		o.setLog(DaoSerializer.getString(_d, "log"));
		o.setSamples(DaoSerializer.getList(_d, "samples", PowerSample.class));
		return o;
	}
}