package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.RequestMessage;

public class NodeNeighborUpdateRequest extends RequestMessage {
	public NodeNeighborUpdateRequest() {
		super(ControllerMessageType.RequestNodeNeighborUpdate, CommandClass.NO_OPERATION, (byte) 0);
	}

	public NodeNeighborUpdateRequest(int _nodeId) {
		super((byte) _nodeId, ControllerMessageType.RequestNodeNeighborUpdate, CommandClass.NO_OPERATION, (byte) 0);
	}
}
