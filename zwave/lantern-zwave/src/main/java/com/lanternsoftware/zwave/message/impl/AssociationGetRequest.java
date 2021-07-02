package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.RequestMessage;

public class AssociationGetRequest extends RequestMessage {
	public AssociationGetRequest() {
		this((byte)0);
	}

	public AssociationGetRequest(byte _nodeId) {
		super(_nodeId, ControllerMessageType.SendData, CommandClass.ASSOCIATION, (byte)0x02);
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId;
	}
}
