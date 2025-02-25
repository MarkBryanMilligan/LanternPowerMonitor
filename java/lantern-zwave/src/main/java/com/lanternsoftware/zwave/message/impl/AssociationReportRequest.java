package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.RequestMessage;

public class AssociationReportRequest extends RequestMessage {
	private byte groupIdx;
	private byte maxAssociations;
	private byte numReportsToFollow;
	private byte[] payload;

	public AssociationReportRequest() {
		this((byte) 0);
	}

	public AssociationReportRequest(byte _nodeId) {
		super(_nodeId, ControllerMessageType.ApplicationCommandHandler, CommandClass.ASSOCIATION, (byte) 0x03);
	}

	@Override
	public void fromPayload(byte[] _payload) {
		nodeId = _payload[5];
		groupIdx = _payload[8];
		maxAssociations = _payload[9];
		numReportsToFollow = _payload[10];
		payload = _payload;
	}

	@Override
	public byte[] getPayload() {
		return payload;
	}

	public byte getGroupIdx() {
		return groupIdx;
	}

	public void setGroupIdx(byte _groupIdx) {
		groupIdx = _groupIdx;
	}

	public byte getMaxAssociations() {
		return maxAssociations;
	}

	public void setMaxAssociations(byte _maxAssociations) {
		maxAssociations = _maxAssociations;
	}

	public byte getNumReportsToFollow() {
		return numReportsToFollow;
	}

	public void setNumReportsToFollow(byte _numReportsToFollow) {
		numReportsToFollow = _numReportsToFollow;
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId;
	}
}
