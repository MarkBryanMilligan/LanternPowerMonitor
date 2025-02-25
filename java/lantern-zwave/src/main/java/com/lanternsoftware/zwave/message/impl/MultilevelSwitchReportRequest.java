package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.RequestMessage;

public class MultilevelSwitchReportRequest extends RequestMessage {
	private int level;

	public MultilevelSwitchReportRequest() {
		super(ControllerMessageType.ApplicationCommandHandler, CommandClass.SWITCH_MULTILEVEL, (byte) 0x03);
	}

	@Override
	public void fromPayload(byte[] _payload) {
		nodeId = _payload[5];
		level = _payload[9];
	}

	public int getLevel() {
		return level;
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId + " level: " + level;
	}
}
