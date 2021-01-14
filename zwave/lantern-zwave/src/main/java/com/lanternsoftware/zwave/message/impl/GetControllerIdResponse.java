package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.NoCommandResponseMessage;

public class GetControllerIdResponse extends NoCommandResponseMessage {
	private long homeId;
	private int controllerId;

	public GetControllerIdResponse() {
		super(ControllerMessageType.MemoryGetId);
	}

	@Override
	public void fromPayload(byte[] _payload) {
		homeId = (getByte(_payload, 4) << 24) | (getByte(_payload, 5) << 16) | (getByte(_payload, 6) << 8) | getByte(_payload, 7);
		controllerId = _payload[8];
	}

	public long getHomeId() {
		return homeId;
	}

	public void setHomeId(int _homeId) {
		homeId = _homeId;
	}

	public int getControllerId() {
		return controllerId;
	}

	public void setControllerId(int _controllerId) {
		controllerId = _controllerId;
	}

	private long getByte(byte[] _data, int _offset) {
		return _data[_offset] & 0xFF;
	}
}
