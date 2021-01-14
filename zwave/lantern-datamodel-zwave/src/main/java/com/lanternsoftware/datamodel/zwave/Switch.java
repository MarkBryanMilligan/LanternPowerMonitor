package com.lanternsoftware.datamodel.zwave;


import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.List;

@DBSerializable
public class Switch {
	private String room;
	private String name;
	private int nodeId;
	private int level;
	private boolean primary;
	private boolean multilevel;
	private boolean hold;
	private String thermostatSource;
	private ThermostatMode thermostatMode;
	private int lowLevel;
	private List<SwitchSchedule> schedule;

	public Switch() {
	}

	public Switch(String _room, String _name, int _nodeId, boolean _primary, boolean _multilevel, String _thermostatSource, int _lowLevel) {
		this(_room, _name, _nodeId, 0, _primary, _multilevel, false, _thermostatSource, _lowLevel, null);
	}

	public Switch(String _room, String _name, int _nodeId, int _level, boolean _primary, boolean _multilevel, boolean _hold, String _thermostatSource, int _lowLevel, List<SwitchSchedule> _schedule) {
		room = _room;
		name = _name;
		nodeId = _nodeId;
		level = _level;
		primary = _primary;
		multilevel = _multilevel;
		hold = _hold;
		thermostatSource = _thermostatSource;
		lowLevel = _lowLevel;
		schedule = _schedule;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String _room) {
		room = _room;
	}

	public String getName() {
		return name;
	}

	public void setName(String _name) {
		name = _name;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int _nodeId) {
		nodeId = _nodeId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int _level) {
		level = _level;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean _primary) {
		primary = _primary;
	}

	public boolean isMultilevel() {
		return multilevel;
	}

	public void setMultilevel(boolean _multilevel) {
		multilevel = _multilevel;
	}

	public boolean isHold() {
		return hold;
	}

	public void setHold(boolean _hold) {
		hold = _hold;
	}

	public String getThermostatSource() {
		return thermostatSource;
	}

	public void setThermostatSource(String _thermostatSource) {
		thermostatSource = _thermostatSource;
	}

	public boolean isThermostat() {
		return NullUtils.isNotEmpty(thermostatSource) && (nodeId < 100);
	}

	public boolean isThermometer() {
		return isUrlThermostat() && (nodeId > 99);
	}

	public boolean isUrlThermostat() {
		return NullUtils.makeNotNull(thermostatSource).startsWith("http");
	}

	public boolean isZWaveThermostat() {
		return NullUtils.isEqual(thermostatSource, "ZWAVE");
	}

	public ThermostatMode getThermostatMode() {
		return thermostatMode;
	}

	public void setThermostatMode(ThermostatMode _thermostatMode) {
		thermostatMode = _thermostatMode;
	}

	public int getLowLevel() {
		return lowLevel;
	}

	public void setLowLevel(int _lowLevel) {
		lowLevel = _lowLevel;
	}

	public List<SwitchSchedule> getSchedule() {
		return schedule;
	}

	public void setSchedule(List<SwitchSchedule> _schedule) {
		schedule = _schedule;
	}
}
