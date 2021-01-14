package com.lanternsoftware.zwave.message;

public abstract class SendDataRequestMessage extends RequestMessage {
	public SendDataRequestMessage(CommandClass _commandClass, byte _command) {
		this((byte)0, _commandClass, _command);
	}

	public SendDataRequestMessage(byte _nodeId, CommandClass _commandClass, byte _command) {
		super(_nodeId, ControllerMessageType.SendData, _commandClass, _command);
	}
}
