package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.MessageUtil;
import com.lanternsoftware.zwave.message.RequestMessage;

public class MultilevelSensorReportRequest extends RequestMessage {
	private double temperature;

	public MultilevelSensorReportRequest() {
		this((byte) 0);
	}

	public MultilevelSensorReportRequest(byte _nodeId) {
		super(_nodeId, ControllerMessageType.ApplicationCommandHandler, CommandClass.SENSOR_MULTILEVEL, (byte) 0x05);
	}

	@Override
	public void fromPayload(byte[] _payload) {
		nodeId = _payload[5];
		if (_payload[9] == (byte) 1)
			temperature = MessageUtil.getTemperatureCelsius(_payload, 10);
	}

	public double getTemperatureCelsius() {
		return temperature;
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId + " level: " + temperature;
	}
}
