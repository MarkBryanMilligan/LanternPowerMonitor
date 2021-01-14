package com.lanternsoftware.datamodel.zwave.dao;

import com.lanternsoftware.datamodel.zwave.Switch;
import com.lanternsoftware.datamodel.zwave.SwitchSchedule;
import com.lanternsoftware.datamodel.zwave.ThermostatMode;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;

import java.util.Collections;
import java.util.List;

public class SwitchSerializer extends AbstractDaoSerializer<Switch>
{
	@Override
	public Class<Switch> getSupportedClass()
	{
		return Switch.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(Switch _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("room", _o.getRoom());
		d.put("name", _o.getName());
		d.put("node_id", _o.getNodeId());
		d.put("level", _o.getLevel());
		d.put("primary", _o.isPrimary());
		d.put("multilevel", _o.isMultilevel());
		d.put("hold", _o.isHold());
		d.put("thermostat_source", _o.getThermostatSource());
		d.put("thermostat_mode", DaoSerializer.toEnumName(_o.getThermostatMode()));
		d.put("low_level", _o.getLowLevel());
		d.put("schedule", DaoSerializer.toDaoEntities(_o.getSchedule(), DaoProxyType.MONGO));
		return d;
	}

	@Override
	public Switch fromDaoEntity(DaoEntity _d)
	{
		Switch o = new Switch();
		o.setRoom(DaoSerializer.getString(_d, "room"));
		o.setName(DaoSerializer.getString(_d, "name"));
		o.setNodeId(DaoSerializer.getInteger(_d, "node_id"));
		o.setLevel(DaoSerializer.getInteger(_d, "level"));
		o.setPrimary(DaoSerializer.getBoolean(_d, "primary"));
		o.setMultilevel(DaoSerializer.getBoolean(_d, "multilevel"));
		o.setHold(DaoSerializer.getBoolean(_d, "hold"));
		o.setThermostatSource(DaoSerializer.getString(_d, "thermostat_source"));
		o.setThermostatMode(DaoSerializer.getEnum(_d, "thermostat_mode", ThermostatMode.class));
		o.setLowLevel(DaoSerializer.getInteger(_d, "low_level"));
		o.setSchedule(DaoSerializer.getList(_d, "schedule", SwitchSchedule.class));
		return o;
	}
}