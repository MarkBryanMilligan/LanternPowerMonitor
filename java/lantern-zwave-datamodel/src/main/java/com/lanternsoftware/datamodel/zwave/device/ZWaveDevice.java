package com.lanternsoftware.datamodel.zwave.device;

import java.util.List;

public abstract class ZWaveDevice {
	private final List<ZWaveSetting> settings;

	public ZWaveDevice(List<ZWaveSetting> _settings) {
		settings = _settings;
	}

	public List<ZWaveSetting> getSettings() {
		return settings;
	}
}
