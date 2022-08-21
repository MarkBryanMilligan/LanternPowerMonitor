package com.lanternsoftware.datamodel.currentmonitor.hub.dao;

import com.lanternsoftware.datamodel.currentmonitor.hub.PowerSample;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class PowerSampleSerializer extends AbstractDaoSerializer<PowerSample>
{
	@Override
	public Class<PowerSample> getSupportedClass()
	{
		return PowerSample.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(PowerSample _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("nano_time", _o.getNanoTime());
		d.put("cycle", _o.getCycle());
		d.put("voltage", _o.getVoltage());
		d.put("current", _o.getCurrent());
		return d;
	}

	@Override
	public PowerSample fromDaoEntity(DaoEntity _d)
	{
		PowerSample o = new PowerSample();
		o.setNanoTime(DaoSerializer.getLong(_d, "nano_time"));
		o.setCycle(DaoSerializer.getInteger(_d, "cycle"));
		o.setVoltage(DaoSerializer.getDouble(_d, "voltage"));
		o.setCurrent(DaoSerializer.getDouble(_d, "current"));
		return o;
	}
}