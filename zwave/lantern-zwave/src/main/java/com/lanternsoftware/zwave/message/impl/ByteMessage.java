package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.NoCommandRequestMessage;

public class ByteMessage extends NoCommandRequestMessage {
	private byte b;

	public ByteMessage() {
		super(ControllerMessageType.None);
	}

	public ByteMessage(byte _b) {
		super(ControllerMessageType.None);
		b = _b;
	}

	public byte getByte() {
		return b;
	}

	public void setByte(byte _b) {
		b = _b;
	}

	@Override
	public byte[] toByteArray(byte _transmitOptions, byte _callbackId) {
		return asByteArray(b);
	}
}
