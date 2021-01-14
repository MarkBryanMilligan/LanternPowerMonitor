package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.SendDataRequestMessage;

public class ThermostatSetPointSupportedGetRequest extends SendDataRequestMessage {
	public ThermostatSetPointSupportedGetRequest() {
		this((byte) 0);
	}

	public ThermostatSetPointSupportedGetRequest(byte _nodeId) {
		super(_nodeId, CommandClass.THERMOSTAT_SETPOINT, (byte) 0x04);
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId;
	}
}
