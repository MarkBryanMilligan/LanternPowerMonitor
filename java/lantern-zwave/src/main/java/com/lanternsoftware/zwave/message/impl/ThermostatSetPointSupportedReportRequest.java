package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.RequestMessage;

import java.util.Set;
import java.util.TreeSet;

public class ThermostatSetPointSupportedReportRequest extends RequestMessage {
	private Set<Byte> supportedSetPointIndices;

	public ThermostatSetPointSupportedReportRequest() {
		this((byte) 0);
	}

	public ThermostatSetPointSupportedReportRequest(byte _nodeId) {
		super(_nodeId, ControllerMessageType.ApplicationCommandHandler, CommandClass.THERMOSTAT_SETPOINT, (byte) 0x05);
	}

	@Override
	public void fromPayload(byte[] _payload) {
		supportedSetPointIndices = new TreeSet<>();
		for (int i = 9; i < _payload.length - 1; ++i) {
			for (int bit = 0; bit < 8; ++bit) {
				if ((_payload[i] & (1 << bit)) != 0) {
					supportedSetPointIndices.add((byte)(((i - 9) << 3) + bit));
				}
			}
		}
	}

	public Set<Byte> getSupportedSetPointIndices() {
		return supportedSetPointIndices;
	}

	public boolean isSupportedIndex(byte _index) {
		return CollectionUtils.contains(supportedSetPointIndices, _index);
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId;
	}
}
