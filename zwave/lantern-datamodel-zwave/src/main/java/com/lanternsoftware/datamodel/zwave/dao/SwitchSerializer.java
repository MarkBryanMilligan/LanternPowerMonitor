package com.lanternsoftware.datamodel.zwave.dao;

import com.lanternsoftware.datamodel.zwave.Switch;
import com.lanternsoftware.datamodel.zwave.SwitchSchedule;
import com.lanternsoftware.datamodel.zwave.SwitchType;
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
		d.put("type", DaoSerializer.toEnumName(_o.getType()));
		d.put("room", _o.getRoom());
		d.put("name", _o.getName());
		d.put("node_id", _o.getNodeId());
		d.put("parent_node_id", _o.getParentNodeId());
		d.put("level", _o.getLevel());
		d.put("gpio_pin", _o.getGpioPin());
		d.put("primary", _o.isPrimary());
		d.put("hold", _o.isHold());
		d.put("hidden", _o.isHidden());
		d.put("thermometer_url", _o.getThermometerUrl());
		d.put("controller_url", _o.getControllerUrl());
		d.put("thermostat_mode", DaoSerializer.toEnumName(_o.getThermostatMode()));
		d.put("low_level", _o.getLowLevel());
		d.put("schedule", DaoSerializer.toDaoEntities(_o.getSchedule(), DaoProxyType.MONGO));
		return d;
	}

	@Override
	public Switch fromDaoEntity(DaoEntity _d)
	{
		Switch o = new Switch();
		o.setType(DaoSerializer.getEnum(_d, "type", SwitchType.class));
		o.setRoom(DaoSerializer.getString(_d, "room"));
		o.setName(DaoSerializer.getString(_d, "name"));
		o.setNodeId(DaoSerializer.getInteger(_d, "node_id"));
		o.setParentNodeId(DaoSerializer.getInteger(_d, "parent_node_id"));
		o.setLevel(DaoSerializer.getInteger(_d, "level"));
		o.setGpioPin(DaoSerializer.getInteger(_d, "gpio_pin"));
		o.setPrimary(DaoSerializer.getBoolean(_d, "primary"));
		o.setHold(DaoSerializer.getBoolean(_d, "hold"));
		o.setHidden(DaoSerializer.getBoolean(_d, "hidden"));
		o.setThermometerUrl(DaoSerializer.getString(_d, "thermometer_url"));
		o.setControllerUrl(DaoSerializer.getString(_d, "controller_url"));
		o.setThermostatMode(DaoSerializer.getEnum(_d, "thermostat_mode", ThermostatMode.class));
		o.setLowLevel(DaoSerializer.getInteger(_d, "low_level"));
		o.setSchedule(DaoSerializer.getList(_d, "schedule", SwitchSchedule.class));
		return o;
	}
}