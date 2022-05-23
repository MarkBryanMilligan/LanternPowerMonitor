package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPolarity;
import com.lanternsoftware.datamodel.currentmonitor.BreakerType;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class BreakerSerializer extends AbstractDaoSerializer<Breaker>
{
	@Override
	public Class<Breaker> getSupportedClass()
	{
		return Breaker.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(Breaker _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("panel", _o.getPanel());
		d.put("space", _o.getSpace());
		d.put("meter", _o.getMeter());
		d.put("hub", _o.getHub());
		d.put("port", _o.getPort());
		d.put("name", _o.getName());
		d.put("description", _o.getDescription());
		d.put("size_amps", _o.getSizeAmps());
		d.put("calibration_factor", _o.getCalibrationFactor());
		d.put("low_pass_filter", _o.getLowPassFilter());
		d.put("polarity", DaoSerializer.toEnumName(_o.getPolarity()));
		d.put("double_power", _o.isDoublePower());
		d.put("type", DaoSerializer.toEnumName(_o.getType()));
		d.put("main", _o.isMain());
		return d;
	}

	@Override
	public Breaker fromDaoEntity(DaoEntity _d)
	{
		Breaker o = new Breaker();
		o.setPanel(DaoSerializer.getInteger(_d, "panel"));
		o.setSpace(DaoSerializer.getInteger(_d, "space"));
		o.setMeter(DaoSerializer.getInteger(_d, "meter"));
		o.setHub(DaoSerializer.getInteger(_d, "hub"));
		o.setPort(DaoSerializer.getInteger(_d, "port"));
		o.setName(DaoSerializer.getString(_d, "name"));
		o.setDescription(DaoSerializer.getString(_d, "description"));
		o.setSizeAmps(DaoSerializer.getInteger(_d, "size_amps"));
		o.setCalibrationFactor(DaoSerializer.getDouble(_d, "calibration_factor"));
		o.setLowPassFilter(DaoSerializer.getDouble(_d, "low_pass_filter"));
		o.setPolarity(DaoSerializer.getEnum(_d, "polarity", BreakerPolarity.class));
		o.setDoublePower(DaoSerializer.getBoolean(_d, "double_power"));
		o.setType(DaoSerializer.getEnum(_d, "type", BreakerType.class));
		o.setMain(DaoSerializer.getBoolean(_d, "main"));
		return o;
	}
}