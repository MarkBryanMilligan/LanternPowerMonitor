package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.SendDataRequestMessage;

public class MultilevelSwitchSetRequest extends SendDataRequestMessage {
	private int level;

	public MultilevelSwitchSetRequest() {
		this(99);
	}

	public MultilevelSwitchSetRequest(int _level) {
		this((byte)0, _level);
	}

	public MultilevelSwitchSetRequest(byte _nodeId, int _level) {
		super(_nodeId, CommandClass.SWITCH_MULTILEVEL, (byte) 0x01);
		level = _level;
	}

	@Override
	public byte[] getPayload() {
		return asByteArray(level);
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId + " level: " + level;
	}
}
