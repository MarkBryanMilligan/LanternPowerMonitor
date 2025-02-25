package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.SendDataRequestMessage;

public class AssociationSetRequest extends SendDataRequestMessage {
	private byte groupIdx;
	private byte targetNodeId;

	public AssociationSetRequest() {
		this((byte)0, (byte)0, (byte)0);
	}

	public AssociationSetRequest(byte _nodeId, byte _groupIdx, byte _targetNodeId) {
		super(_nodeId, CommandClass.ASSOCIATION, (byte) 0x01);
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
