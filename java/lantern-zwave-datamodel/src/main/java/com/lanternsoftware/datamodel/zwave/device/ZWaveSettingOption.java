package com.lanternsoftware.datamodel.zwave.device;

public abstract class ZWaveSettingOption {
	private final String description;
	private final byte[] value;

	public ZWaveSettingOption(String _description, byte[] _value) {
		description = _description;
		value = _value;
	}

	public String getDescription() {
		return description;
	}

	public byte[] getValue() {
		return value;
	}
}
