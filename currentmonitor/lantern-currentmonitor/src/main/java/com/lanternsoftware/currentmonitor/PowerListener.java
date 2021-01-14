package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.datamodel.currentmonitor.BreakerPower;

public interface PowerListener {
	void onPowerEvent(BreakerPower _power);
}
