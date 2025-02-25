package com.lanternsoftware.datamodel.zwave.device;

import java.util.List;

public abstract class ZWaveSetting {
	private final int parameter;
	private final List<ZWaveSettingOption> options;

	public ZWaveSetting(int _parameter, List<ZWaveSettingOption> _options) {
		parameter = _parameter;
		options = _options;
	}

	public int getParameter() {
		return parameter;
	}

	public List<ZWaveSettingOption> getOptions() {
		return options;
	}
}
