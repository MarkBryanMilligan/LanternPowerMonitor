package com.lanternsoftware.zwave.message;

public abstract class RequestMessage extends Message {
	public RequestMessage(ControllerMessageType _controllerMessageType, CommandClass _commandClass, byte _command) {
		this((byte)0, _controllerMessageType, _commandClass, _command);
	}

	public RequestMessage(byte _nodeId, ControllerMessageType _controllerMessageType, CommandClass _commandClass, byte _command) {
		super(_nodeId, _controllerMessageType, MessageType.REQUEST, _commandClass, _command);
	}
}
