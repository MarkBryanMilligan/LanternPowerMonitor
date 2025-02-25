package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.SendDataRequestMessage;

public class AssociationRemoveRequest extends SendDataRequestMessage {
	private byte groupIdx;
	private byte targetNodeId;

	public AssociationRemoveRequest() {
		this((byte)0, (byte)0, (byte)0);
	}

	public AssociationRemoveRequest(byte _nodeId, byte _groupIdx, byte _targetNodeId) {
		super(_nodeId, CommandClass.ASSOCIATION, (byte) 0x04);
		groupIdx = _groupIdx;
		targetNodeId = _targetNodeId;
	}

	public byte getGroupIdx() {
		return groupIdx;
	}

	public void setGroupIdx(byte _groupIdx) {
		groupIdx = _groupIdx;
	}

	public byte getTargetNodeId() {
		return targetNodeId;
	}

	public void setTargetNodeId(byte _targetNodeId) {
		targetNodeId = _targetNodeId;
	}

	@Override
	public byte[] getPayload() {
		return asByteArray(groupIdx, targetNodeId);
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId + " groupIdx: " + groupIdx + " targetNodeIdx: " + targetNodeId;
	}
}
