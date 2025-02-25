package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.RequestMessage;

public class CRC16EncapRequest extends RequestMessage {
	private boolean on;

	public CRC16EncapRequest() {
		super(ControllerMessageType.ApplicationCommandHandler, CommandClass.CRC_16_ENCAP, (byte) 0x03);
	}

	@Override
	public void fromPayload(byte[] _payload) {
		nodeId = _payload[5];
		on = _payload[11] == 1;
	}

	public boolean isOn() {
		return on;
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId + " on: " + on;
	}
}
