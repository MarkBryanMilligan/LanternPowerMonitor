package com.lanternsoftware.zwave.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class Message {
	protected byte nodeId;
	protected final ControllerMessageType controllerMessageType;
	protected final MessageType messageType;
	protected final CommandClass commandClass;
	protected final byte command;

	public Message(ControllerMessageType _controllerMessageType, MessageType _messageType, CommandClass _commandClass, byte _command) {
		this((byte) 0, _controllerMessageType, _messageType, _commandClass, _command);
	}

	public Message(byte _nodeId, ControllerMessageType _controllerMessageType, MessageType _messageType, CommandClass _commandClass, byte _command) {
		nodeId = _nodeId;
		controllerMessageType = _controllerMessageType;
		messageType = _messageType;
		commandClass = _commandClass;
		command = _command;
	}

	public byte getNodeId() {
		return nodeId;
	}

	public void setNodeId(byte _nodeId) {
		nodeId = _nodeId;
	}

	public byte[] toPayload() {
		try {
			byte[] payload = getPayload();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			if (nodeId > 0)
				os.write(nodeId);

			if (commandClass != CommandClass.NO_OPERATION) {
				os.write(payload.length + 2);
				os.write(commandClass.data);
				os.write(command);
			}
			if (payload.length > 0)
				os.write(payload);
			os.close();
			return os.toByteArray();
		} catch (IOException _e) {
			_e.printStackTrace();
			return new byte[0];
		}
	}

	public void fromPayload(byte[] _payload) {
	}

	public byte[] getPayload() {
		return new byte[0];
	}

	public byte[] toByteArray(byte _transmitOptions, byte _callbackId) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] payload = toPayload();
			os.write((byte) 0x01);
			int messageLength = payload.length + (isCallbackExpected() ? 5 : 3);
			os.write((byte) messageLength);
			os.write(messageType.data);
			os.write(controllerMessageType.data);
			if (payload.length > 0)
				os.write(payload);

			if (isCallbackExpected()) {
				os.write(_transmitOptions);
				os.write(_callbackId);
			}

			os.write((byte) 1);
			byte[] msg = os.toByteArray();
			msg[msg.length - 1] = calculateChecksum(msg);
			return msg;
		} catch (IOException _e) {
			_e.printStackTrace();
			return null;
		}
	}

	public String getKey() {
		return toKey(controllerMessageType.data, messageType.data, commandClass.data, command);
	}

	public static String toKey(byte _controllerMessageType, byte _messageType, byte _commandClass, byte _command) {
		return String.format("%02X%02X%02X%02X", _controllerMessageType, _messageType, _commandClass, _command);
	}

	public static byte calculateChecksum(byte[] buffer) {
		byte checkSum = (byte) 0xFF;
		for (int i = 1; i < buffer.length - 1; i++) {
			checkSum = (byte) (checkSum ^ buffer[i]);
		}
		return checkSum;
	}

	protected byte[] asByteArray(int _byte) {
		byte[] ret = new byte[1];
		ret[0] = (byte) _byte;
		return ret;
	}

	protected byte[] asByteArray(byte... _bytes) {
		return _bytes;
	}

	public String name() {
		return getClass().getSimpleName();
	}

	public boolean isCallbackExpected() {
		return (controllerMessageType == ControllerMessageType.SendData) && (messageType == MessageType.REQUEST);
	}

	public String describe() {
		return name();
	}
}
