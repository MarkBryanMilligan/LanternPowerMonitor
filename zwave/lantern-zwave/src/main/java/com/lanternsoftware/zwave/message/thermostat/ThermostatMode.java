package com.lanternsoftware.zwave.message.thermostat;

public enum ThermostatMode {
	OFF((byte)0),
	HEAT((byte)1),
	COOL((byte)2),
	AUTO((byte)3),
	AUXILIARY((byte)4);

	public final byte data;

	ThermostatMode(byte _data) {
		data = _data;
	}

	public static ThermostatMode fromByte(byte _bt) {
		for (ThermostatMode mode : values()) {
			if (mode.data == _bt)
				return mode;
		}
		return null;
	}
}
