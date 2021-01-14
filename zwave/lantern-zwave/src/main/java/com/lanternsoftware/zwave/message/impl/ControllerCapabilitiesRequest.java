package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.NoCommandRequestMessage;

public class ControllerCapabilitiesRequest extends NoCommandRequestMessage {
	public ControllerCapabilitiesRequest() {
		super(ControllerMessageType.GetCapabilities);
	}
}
