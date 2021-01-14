package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.SendDataRequestMessage;

public class ThermostatModeGetRequest extends SendDataRequestMessage {
	public ThermostatModeGetRequest() {
		this((byte) 0);
	}

	public ThermostatModeGetRequest(byte _nodeId) {
		super(_nodeId, CommandClass.THERMOSTAT_MODE, (byte) 0x02);
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId;
	}
}
