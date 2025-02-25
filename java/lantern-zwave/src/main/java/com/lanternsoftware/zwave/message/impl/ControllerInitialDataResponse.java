package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.NoCommandResponseMessage;

import java.util.ArrayList;
import java.util.List;

public class ControllerInitialDataResponse extends NoCommandResponseMessage {
	private boolean master;
	private boolean primary;
	private List<Byte> nodeIds;

	public ControllerInitialDataResponse() {
		super(ControllerMessageType.SerialApiGetInitData);
	}

	@Override
	public void fromPayload(byte[] _payload) {
		int length = _payload[6];
		if (length == 29) {
			byte nodeId = 1;
			nodeIds = new ArrayList<>();
			for (int i = 7; i < 7 + length; i++) {
				byte curByte = _payload[i];
				for (int j = 0; j < 8; j++) {
					int bit = 1 << j;
					if ((curByte & bit) == bit) {
						nodeIds.add(nodeId);
					}
					nodeId++;
				}
			}
			master = (_payload[5] & 0x1) == 0;
			primary = (_payload[5] & 0x4) == 0;
		}
	}

	public boolean isMaster() {
		return master;
	}

	public boolean isPrimary() {
		return primary;
	}

	public List<Byte> getNodeIds() {
		return nodeIds;
	}
}
