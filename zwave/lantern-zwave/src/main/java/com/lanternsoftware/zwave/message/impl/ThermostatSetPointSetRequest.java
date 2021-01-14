package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.SendDataRequestMessage;
import com.lanternsoftware.zwave.message.thermostat.ThermostatSetPointIndex;

public class ThermostatSetPointSetRequest extends SendDataRequestMessage {
	private ThermostatSetPointIndex index;
	private int level;

	public ThermostatSetPointSetRequest() {
		this(ThermostatSetPointIndex.HEATING, 72);
	}

	public ThermostatSetPointSetRequest(ThermostatSetPointIndex _index, int _level) {
		this((byte)0, _index, _level);
	}

	public ThermostatSetPointSetRequest(byte _nodeId, ThermostatSetPointIndex _index, int _level) {
		super(_nodeId, CommandClass.THERMOSTAT_SETPOINT, (byte) 0x01);
		index = _index;
		level = _level;
	}

	@Override
	public byte[] getPayload() {
		return asByteArray(index.index, (byte)9, (byte)level);
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId + " level: " + level;
	}
}
