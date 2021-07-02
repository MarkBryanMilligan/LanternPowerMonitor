package com.lanternsoftware.zwave.dao;

import com.lanternsoftware.datamodel.zwave.ZWaveConfig;

public interface ZWaveDao {
	void putConfig(ZWaveConfig _config);
	ZWaveConfig getConfig(int _accountId);
	void shutdown();
}
