package com.lanternsoftware.zwave.message;

public abstract class ResponseMessage extends Message {
	public ResponseMessage(ControllerMessageType _controllerMessageType, CommandClass _commandClass, byte _command) {
		this((byte) 0, _controllerMessageType, _commandClass, _command);
	}

	public ResponseMessage(byte _nodeId, ControllerMessageType _controllerMessageType, CommandClass _commandClass, byte _command) {
		super(_nodeId, _controllerMessageType, MessageType.RESPONSE, _commandClass, _command);
	}
}
