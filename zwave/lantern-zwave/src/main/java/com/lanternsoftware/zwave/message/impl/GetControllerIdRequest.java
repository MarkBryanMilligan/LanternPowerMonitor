package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.NoCommandRequestMessage;

public class GetControllerIdRequest extends NoCommandRequestMessage {
	public GetControllerIdRequest() {
		super(ControllerMessageType.MemoryGetId);
	}
}
