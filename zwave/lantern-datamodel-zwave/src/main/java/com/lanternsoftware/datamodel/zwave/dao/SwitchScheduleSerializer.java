package com.lanternsoftware.datamodel.zwave.dao;

import com.lanternsoftware.datamodel.zwave.SwitchSchedule;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;

import java.util.Collections;
import java.util.List;

public class SwitchScheduleSerializer extends AbstractDaoSerializer<SwitchSchedule>
{
	@Override
	public Class<SwitchSchedule> getSupportedClass()
	{
		return SwitchSchedule.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(SwitchSchedule _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("day_of_week", _o.getDayOfWeek());
		d.put("time_of_day", _o.getTimeOfDay());
		d.put("minutes_per_hour", _o.getMinutesPerHour());
		d.put("level", _o.getLevel());
		return d;
	}

	@Override
	public SwitchSchedule fromDaoEntity(DaoEntity _d)
	{
		SwitchSchedule o = new SwitchSchedule();
		o.setDayOfWeek(DaoSerializer.getInteger(_d, "day_of_week"));
		o.setTimeOfDay(DaoSerializer.getInteger(_d, "time_of_day"));
		o.setMinutesPerHour(DaoSerializer.getInteger(_d, "minutes_per_hour"));
		o.setLevel(DaoSerializer.getInteger(_d, "level"));
		return o;
	}
}