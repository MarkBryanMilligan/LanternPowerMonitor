package com.lanternsoftware.zwave.message;

public enum MessageType {
	REQUEST((byte)0),
	RESPONSE((byte)1);

	public final byte data;

	MessageType(byte _data) {
		data = _data;
	}
}
