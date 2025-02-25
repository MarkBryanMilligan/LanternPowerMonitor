package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.NoCommandRequestMessage;

public class AddNodeToNetworkStopRequest extends NoCommandRequestMessage {
	public AddNodeToNetworkStopRequest() {
		super(ControllerMessageType.AddNodeToNetwork);
	}

	@Override
	public byte[] getPayload() {
		byte[] payload = new byte[1];
		payload[0] = (byte)0x05;
		return payload;
	}
}
