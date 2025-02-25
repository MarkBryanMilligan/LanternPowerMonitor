package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.SendDataRequestMessage;
import com.lanternsoftware.zwave.message.thermostat.ThermostatMode;

public class ThermostatModeSetRequest extends SendDataRequestMessage {
	private ThermostatMode mode;

	public ThermostatModeSetRequest() {
		this((byte) 0, ThermostatMode.OFF);
	}

	public ThermostatModeSetRequest(byte _nodeId, ThermostatMode _mode) {
		super(_nodeId, CommandClass.THERMOSTAT_MODE, (byte) 0x01);
		mode = _mode;
	}

	@Override
	public byte[] getPayload() {
		return asByteArray(mode.data);
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId;
	}
}
