package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.RequestMessage;

public class DeviceManufacturerActionRequest extends RequestMessage {
	public DeviceManufacturerActionRequest() {
		this((byte) 0);
	}

	public DeviceManufacturerActionRequest(byte _nodeId) {
		super(_nodeId, ControllerMessageType.SendData, CommandClass.MANUFACTURER_SPECIFIC, (byte) 0x04);
	}
}
