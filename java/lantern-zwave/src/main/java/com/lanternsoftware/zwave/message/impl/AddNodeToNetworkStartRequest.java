package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.NoCommandRequestMessage;

public class AddNodeToNetworkStartRequest extends NoCommandRequestMessage {
	public AddNodeToNetworkStartRequest() {
		super(ControllerMessageType.AddNodeToNetwork);
	}

	@Override
	public byte[] getPayload() {
		byte[] payload = new byte[1];
		payload[0] = (byte)0xC1;
		return payload;
	}
}
