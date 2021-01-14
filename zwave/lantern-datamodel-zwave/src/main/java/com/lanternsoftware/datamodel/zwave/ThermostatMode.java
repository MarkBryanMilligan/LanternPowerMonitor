package com.lanternsoftware.datamodel.zwave;

public enum ThermostatMode {
	OFF((byte)0, "Off"),
	HEAT((byte)1, "Heat"),
	COOL((byte)2, "Cool"),
	AUXILIARY((byte)4, "E-Heat");

	public final byte data;
	public final String display;

	ThermostatMode(byte _data, String _display) {
		data = _data;
		display = _display;
	}

	public static ThermostatMode fromByte(byte _bt) {
		for (ThermostatMode mode : values()) {
			if (mode.data == _bt)
				return mode;
		}
		return null;
	}
}
