package com.lanternsoftware.powermonitor;

import com.lanternsoftware.powermonitor.datamodel.BreakerPower;
import com.lanternsoftware.powermonitor.datamodel.hub.HubSample;

public interface PowerListener {
	void onPowerEvent(BreakerPower _power);
	void onSampleEvent(HubSample _sample);
}
