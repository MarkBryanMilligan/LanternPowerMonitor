package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.RequestMessage;

public class MultilevelSensorGetRequest extends RequestMessage {
	public MultilevelSensorGetRequest() {
		this((byte)0);
	}

	public MultilevelSensorGetRequest(byte _nodeId) {
		super(_nodeId, ControllerMessageType.SendData, CommandClass.SENSOR_MULTILEVEL, (byte)0x04);
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId;
	}
}
