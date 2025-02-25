package com.lanternsoftware.zwave.message;

public abstract class NoCommandResponseMessage extends ResponseMessage {
	public NoCommandResponseMessage(ControllerMessageType _controllerMessageType) {
		super(_controllerMessageType, CommandClass.NO_OPERATION, (byte) 0);
	}
}
