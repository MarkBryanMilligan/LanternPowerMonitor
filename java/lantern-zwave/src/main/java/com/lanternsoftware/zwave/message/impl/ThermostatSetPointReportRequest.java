package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.MessageUtil;
import com.lanternsoftware.zwave.message.RequestMessage;
import com.lanternsoftware.zwave.message.thermostat.ThermostatSetPointIndex;

public class ThermostatSetPointReportRequest extends RequestMessage {
	private ThermostatSetPointIndex index;
	private double temperature;

	public ThermostatSetPointReportRequest() {
		this((byte) 0);
	}

	public ThermostatSetPointReportRequest(byte _nodeId) {
		super(_nodeId, ControllerMessageType.ApplicationCommandHandler, CommandClass.THERMOSTAT_SETPOINT, (byte) 0x03);
	}

	@Override
	public void fromPayload(byte[] _payload) {
		nodeId = _payload[5];
		index = ThermostatSetPointIndex.fromIndex(_payload[9]);
		if (index != null)
			temperature = MessageUtil.getTemperatureCelsius(_payload, 10);
	}

	public ThermostatSetPointIndex getIndex() {
		return index;
	}

	public double getTemperatureCelsius() {
		return temperature;
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId + " level: " + temperature;
	}
}
