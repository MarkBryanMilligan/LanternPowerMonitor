package com.lanternsoftware.powermonitor.datamodel.dao;

import com.lanternsoftware.powermonitor.datamodel.BreakerHub;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class BreakerHubSerializer extends AbstractDaoSerializer<BreakerHub>
{
	@Override
	public Class<BreakerHub> getSupportedClass()
	{
		return BreakerHub.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(BreakerHub _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("hub", _o.getHub());
		d.put("voltage_calibration_factor", _o.getRawVoltageCalibrationFactor());
		d.put("port_calibration_factor", _o.getRawPortCalibrationFactor());
		d.put("phase_cnt", _o.getPhaseCnt());
		d.put("phase_offset_ns", _o.getPhaseOffsetNs());
		d.put("frequency", _o.getFrequency());
		d.put("bluetooth_mac", _o.getBluetoothMac());
		return d;
	}

	@Override
	public BreakerHub fromDaoEntity(DaoEntity _d)
	{
		BreakerHub o = new BreakerHub();
		o.setHub(DaoSerializer.getInteger(_d, "hub"));
		o.setVoltageCalibrationFactor(DaoSerializer.getDouble(_d, "voltage_calibration_factor"));
		o.setPortCalibrationFactor(DaoSerializer.getDouble(_d, "port_calibration_factor"));
		o.setPhaseCnt(DaoSerializer.getInteger(_d, "phase_cnt"));
		o.setPhaseOffsetNs(DaoSerializer.getInteger(_d, "phase_offset_ns"));
		o.setFrequency(DaoSerializer.getInteger(_d, "frequency"));
		o.setBluetoothMac(DaoSerializer.getString(_d, "bluetooth_mac"));
		return o;
	}
}