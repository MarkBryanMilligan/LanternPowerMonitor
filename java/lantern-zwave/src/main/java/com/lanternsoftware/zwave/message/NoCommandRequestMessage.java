package com.lanternsoftware.zwave.message;

public abstract class NoCommandRequestMessage extends RequestMessage {
	public NoCommandRequestMessage(ControllerMessageType _controllerMessageType) {
		this((byte)0, _controllerMessageType);
	}

	public NoCommandRequestMessage(byte _nodeId, ControllerMessageType _controllerMessageType) {
		super(_nodeId, _controllerMessageType, CommandClass.NO_OPERATION, (byte) 0);
	}
}
