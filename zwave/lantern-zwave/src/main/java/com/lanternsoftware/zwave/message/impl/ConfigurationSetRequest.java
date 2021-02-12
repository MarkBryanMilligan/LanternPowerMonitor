package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.SendDataRequestMessage;

public class ConfigurationSetRequest extends SendDataRequestMessage {
	private byte parameter;
	private byte[] value;

	public ConfigurationSetRequest() {
		this((byte)0, (byte)0, null);
	}

	public ConfigurationSetRequest(byte _nodeId, byte _parameter, byte[] _value) {
		super(_nodeId, CommandClass.CONFIGURATION, (byte) 0x04);
		parameter = _parameter;
		value = _value;
	}

	public byte getParameter() {
		return parameter;
	}

	public void setParameter(byte _parameter) {
		parameter = _parameter;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] _value) {
		value = _value;
	}

	@Override
	public byte[] getPayload() {
		return CollectionUtils.merge(asByteArray(parameter), asByteArray((byte)CollectionUtils.length(value)), value);
	}

	@Override
	public String describe() {
		return name() + " node: " + nodeId + " parameter: " + parameter + " value: " + NullUtils.toHex(value);
	}
}
