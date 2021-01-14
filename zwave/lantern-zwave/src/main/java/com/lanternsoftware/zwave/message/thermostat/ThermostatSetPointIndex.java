package com.lanternsoftware.zwave.message.thermostat;

public enum ThermostatSetPointIndex {
	HEATING((byte)1),
	COOLING((byte)2),
	FURNACE((byte)7),
	DRY_AIR((byte)8),
	MOIST_AIR((byte)9),
	AUTO_CHANGEOVER((byte)10),
	HEATING_ECON((byte)11),
	COOLING_ECON((byte)12),
	AWAY_HEATING((byte)13),
	COOLING_HEATING((byte)14),
	HEATING_MINIMUM((byte)101),
	COOLING_MINIMUM((byte)102),
	FURNACE_MINIMUM((byte)107),
	DRY_AIR_MINIMUM((byte)108),
	MOIST_AIR_MINIMUM((byte)109),
	AUTO_CHANGEOVER_MINIMUM((byte)110),
	HEATING_ECON_MINIMUM((byte)111),
	COOLING_ECON_MINIMUM((byte)112),
	AWAY_HEATING_MINIMUM((byte)113),
	COOLING_HEATING_MINIMUM((byte)114),
	HEATING_MAXIMUM((byte)201),
	COOLING_MAXIMUM((byte)202),
	FURNACE_MAXIMUM((byte)207),
	DRY_AIR_MAXIMUM((byte)208),
	MOIST_AIR_MAXIMUM((byte)209),
	AUTO_CHANGEOVER_MAXIMUM((byte)210),
	HEATING_ECON_MAXIMUM((byte)211),
	COOLING_ECON_MAXIMUM((byte)212),
	AWAY_HEATING_MAXIMUM((byte)213),
	COOLING_HEATING_MAXIMUM((byte)214);

	public final byte index;

	ThermostatSetPointIndex(byte _index) {
		index = _index;
	}

	public static ThermostatSetPointIndex fromIndex(byte _index) {
		for (ThermostatSetPointIndex idx : values()) {
			if (idx.index == _index)
				return idx;
		}
		return null;
	}
}
