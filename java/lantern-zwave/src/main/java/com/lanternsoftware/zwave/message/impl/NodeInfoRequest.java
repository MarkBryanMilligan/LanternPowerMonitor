package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.NoCommandRequestMessage;

public class NodeInfoRequest extends NoCommandRequestMessage {
	public NodeInfoRequest() {
		super(ControllerMessageType.RequestNodeInfo);
	}

	public NodeInfoRequest(byte _nodeId) {
		super(_nodeId, ControllerMessageType.RequestNodeInfo);
	}
}
