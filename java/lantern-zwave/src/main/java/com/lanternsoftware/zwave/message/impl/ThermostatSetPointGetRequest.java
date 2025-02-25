package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.SendDataRequestMessage;
import com.lanternsoftware.zwave.message.thermostat.ThermostatSetPointIndex;

public class ThermostatSetPointGetRequest extends SendDataRequestMessage {
	private ThermostatSetPointIndex index;

	public ThermostatSetPointGetRequest() {
		this((byte) 0, ThermostatSetPointIndex.HEATING);
	}

	public ThermostatSetPointGetRequest(byte _nodeId, ThermostatSetPointIndex _index) {
		super(_nodeId, CommandClass.THERMOSTAT_SETPOINT, (byte) 0x02);
		index = _index;
	}

	@Override
	public byte[] getPayload() {
		return asByteArray(index.index);
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId;
	}
}
