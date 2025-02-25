package com.lanternsoftware.powermonitor.datamodel.dao;

import com.lanternsoftware.powermonitor.datamodel.BreakerPowerMinute;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class BreakerPowerMinuteSerializer extends AbstractDaoSerializer<BreakerPowerMinute> {
	@Override
	public Class<BreakerPowerMinute> getSupportedClass() {
		return BreakerPowerMinute.class;
	}

	@Override
	public DaoEntity toDaoEntity(BreakerPowerMinute _o) {
		DaoEntity d = new DaoEntity();
		d.put("panel", _o.getPanel());
		d.put("space", _o.getSpace());
		ByteBuffer bb = ByteBuffer.allocate(240);
		for (Float reading : CollectionUtils.makeNotNull(_o.getReadings())) {
			bb.putFloat(DaoSerializer.toFloat(reading));
		}
		d.put("readings", bb.array());
		return d;
	}

	@Override
	public BreakerPowerMinute fromDaoEntity(DaoEntity _d) {
		BreakerPowerMinute o = new BreakerPowerMinute();
		o.setPanel(DaoSerializer.getInteger(_d, "panel"));
		o.setSpace(DaoSerializer.getInteger(_d, "space"));
		byte[] data = DaoSerializer.getByteArray(_d, "readings");
		List<Float> readings = new ArrayList<>();
		o.setReadings(readings);
		if (CollectionUtils.length(data) > 0) {
			ByteBuffer bb = ByteBuffer.wrap(data);
			while (bb.hasRemaining()) {
				readings.add(bb.getFloat());
			}
		}
		return o;
	}
}
