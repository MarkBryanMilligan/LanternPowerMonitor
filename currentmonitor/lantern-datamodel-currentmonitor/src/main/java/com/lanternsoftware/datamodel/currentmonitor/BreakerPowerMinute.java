package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.List;

@DBSerializable(autogen = false)
public class BreakerPowerMinute {
	private int panel;
	private int space;
	private List<Float> readings;

	public int getPanel() {
		return panel;
	}

	public void setPanel(int _panel) {
		panel = _panel;
	}

	public int getSpace() {
		return space;
	}

	public void setSpace(int _space) {
		space = _space;
	}

	public String breakerKey() {
		return Breaker.key(panel, space);
	}

	public List<Float> getReadings() {
		return readings;
	}

	public void setReadings(List<Float> _readings) {
		readings = _readings;
	}
}
