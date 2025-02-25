package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.NoCommandResponseMessage;

public class NodeInfoResponse extends NoCommandResponseMessage {
	public NodeInfoResponse() {
		super(ControllerMessageType.RequestNodeInfo);
	}
}
