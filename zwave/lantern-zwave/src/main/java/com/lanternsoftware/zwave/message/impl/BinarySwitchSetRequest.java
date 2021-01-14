package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.SendDataRequestMessage;

public class BinarySwitchSetRequest extends SendDataRequestMessage {
	private boolean on;

	public BinarySwitchSetRequest() {
		this(true);
	}

	public BinarySwitchSetRequest(boolean _on) {
		this((byte)0, _on);
	}

	public BinarySwitchSetRequest(byte _nodeId, boolean _on) {
		super(_nodeId, CommandClass.SWITCH_BINARY, (byte)0x01);
		on = _on;
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean _on) {
		on = _on;
	}

	@Override
	public byte[] getPayload() {
		return asByteArray(on?0xFF:0);
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId + " on: " + on;
	}
}
