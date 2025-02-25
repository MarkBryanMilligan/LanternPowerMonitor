package com.lanternsoftware.datamodel.zwave;


import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.List;
import java.util.Objects;

@DBSerializable
public class Switch {
	private SwitchType type;
	private String room;
	private String name;
	private int nodeId;
	private int parentNodeId;
	private int level;
	private int gpioPin;
	private boolean primary;
	private boolean hold;
	private boolean hidden;
	private boolean suppressEvents;
	private String sourceUrl;
	private String controllerUrl;
	private ThermostatMode thermostatMode;
	private int lowLevel;
	private List<SwitchSchedule> schedule;

	public Switch() {
	}

	public Switch(String _room, String _name, int _nodeId, boolean _primary, boolean _multilevel, String _sourceUrl, int _lowLevel) {
		this(_room, _name, _nodeId, 0, _primary, false, _sourceUrl, _lowLevel, null);
	}

	public Switch(String _room, String _name, int _nodeId, int _level, boolean _primary, boolean _hold, String _sourceUrl, int _lowLevel, List<SwitchSchedule> _schedule) {
		room = _room;
		name = _name;
		nodeId = _nodeId;
		level = _level;
		primary = _primary;
		hold = _hold;
		sourceUrl = _sourceUrl;
		lowLevel = _lowLevel;
		schedule = _schedule;
	}

	public SwitchType getType() {
		return type;
	}

	public void setType(SwitchType _type) {
		type = _type;
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

	public String getFullDisplay() {
		if (NullUtils.isNotEmpty(room))
			return room + " - " + name;
		return name;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int _nodeId) {
		nodeId = _nodeId;
	}

	public int getParentNodeId() {
		return parentNodeId;
	}

	public void setParentNodeId(int _parentNodeId) {
		parentNodeId = _parentNodeId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int _level) {
		level = _level;
	}

	public int getGpioPin() {
		return gpioPin;
	}

	public void setGpioPin(int _gpioPin) {
		gpioPin = _gpioPin;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean _primary) {
		primary = _primary;
	}

	public boolean isMultilevel() {
		return type == SwitchType.DIMMER;
	}

	public boolean isHold() {
		return hold;
	}

	public void setHold(boolean _hold) {
		hold = _hold;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String _sourceUrl) {
		sourceUrl = _sourceUrl;
	}

	public String getControllerUrl() {
		return controllerUrl;
	}

	public void setControllerUrl(String _controllerUrl) {
		controllerUrl = _controllerUrl;
	}

	public boolean isThermostat() {
		return isSpaceHeaterThermostat() || isZWaveThermostat();
	}

	public boolean isSpaceHeaterThermostat() {
		return type == SwitchType.SPACE_HEATER_THERMOSTAT;
	}

	public boolean isSourceUrlValid() {
		return NullUtils.makeNotNull(sourceUrl).startsWith("http");
	}

	public boolean isZWaveThermostat() {
		return type == SwitchType.THERMOSTAT;
	}

	public boolean isRelay() {
		return type == SwitchType.RELAY;
	}

	public boolean isRelayButton() {
		return type == SwitchType.RELAY_BUTTON;
	}

	public boolean isSecurity() {
		return type == SwitchType.SECURITY;
	}

	public boolean isControlledBy(String _controllerUrl) {
		return NullUtils.isEqual(_controllerUrl, controllerUrl);
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

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean _hidden) {
		hidden = _hidden;
	}

	public boolean isSuppressEvents() {
		return suppressEvents;
	}

	public void setSuppressEvents(boolean _suppressEvents) {
		suppressEvents = _suppressEvents;
	}

	public List<SwitchSchedule> getSchedule() {
		return schedule;
	}

	public void setSchedule(List<SwitchSchedule> _schedule) {
		schedule = _schedule;
	}

	@Override
	public boolean equals(Object _o) {
		if (this == _o) return true;
		if (_o == null || getClass() != _o.getClass()) return false;
		Switch aSwitch = (Switch) _o;
		return nodeId == aSwitch.nodeId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nodeId);
	}

	public boolean isModified(Switch _switch) {
		return (_switch == null) || (level != _switch.getLevel()) || (hold != _switch.isHold()) || (thermostatMode != _switch.getThermostatMode());
	}

	public Switch duplicate() {
		Switch s = new Switch();
		s.setType(getType());
		s.setRoom(getRoom());
		s.setName(getName());
		s.setNodeId(getNodeId());
		s.setLevel(getLevel());
		s.setGpioPin(getGpioPin());
		s.setPrimary(isPrimary());
		s.setHold(isHold());
		s.setHidden(isHidden());
		s.setSourceUrl(getSourceUrl());
		s.setControllerUrl(getControllerUrl());
		s.setThermostatMode(getThermostatMode());
		s.setLowLevel(getLowLevel());
		s.setSchedule(CollectionUtils.transform(getSchedule(), SwitchSchedule::duplicate));
		return s;
	}
}
