package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.ResponseMessage;

public class SendDataResponse extends ResponseMessage {
	private byte response;

	public SendDataResponse() {
		this((byte) 0);
	}

	public SendDataResponse(byte _response) {
		super(ControllerMessageType.SendData, CommandClass.NO_OPERATION, (byte) 0);
		response = _response;
	}

	@Override
	public void fromPayload(byte[] _payload) {
		if (CollectionUtils.length(_payload) > 0)
			response = _payload[0];
	}

	public boolean isSuccess() {
		return response != 0;
	}

	public String describe() {
		return name() + ": " + (isSuccess() ? "SUCCESS" : "FAILURE");
	}
}
