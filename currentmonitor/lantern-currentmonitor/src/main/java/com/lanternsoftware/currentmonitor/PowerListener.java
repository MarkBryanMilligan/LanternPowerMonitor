package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.datamodel.currentmonitor.BreakerPower;
import com.lanternsoftware.datamodel.currentmonitor.hub.HubSample;

public interface PowerListener {
	void onPowerEvent(BreakerPower _power);
	void onSampleEvent(HubSample _sample);
}
