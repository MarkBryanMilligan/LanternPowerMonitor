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
		d.put("time_of_day_end", _o.getTimeOfDayEnd());
		d.put("on_duration", _o.getOnDuration());
		d.put("off_duration", _o.getOffDuration());
		d.put("level", _o.getLevel());
		return d;
	}

	@Override
	public SwitchSchedule fromDaoEntity(DaoEntity _d)
	{
		SwitchSchedule o = new SwitchSchedule();
		o.setDayOfWeek(DaoSerializer.getInteger(_d, "day_of_week"));
		o.setTimeOfDay(DaoSerializer.getInteger(_d, "time_of_day"));
		o.setTimeOfDayEnd(DaoSerializer.getInteger(_d, "time_of_day_end"));
		o.setOnDuration(DaoSerializer.getInteger(_d, "on_duration"));
		o.setOffDuration(DaoSerializer.getInteger(_d, "off_duration"));
		o.setLevel(DaoSerializer.getInteger(_d, "level"));
		return o;
	}
}