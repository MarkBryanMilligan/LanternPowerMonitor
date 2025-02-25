package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.NoCommandRequestMessage;

public class ControllerInitialDataRequest extends NoCommandRequestMessage {
	public ControllerInitialDataRequest() {
		super(ControllerMessageType.SerialApiGetInitData);
	}
}
