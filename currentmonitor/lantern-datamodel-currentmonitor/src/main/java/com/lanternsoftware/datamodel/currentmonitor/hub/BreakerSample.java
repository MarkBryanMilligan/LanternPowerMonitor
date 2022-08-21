package com.lanternsoftware.datamodel.currentmonitor.hub;

import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.List;

@DBSerializable
public class BreakerSample {
	private int panel;
	private int space;
	private List<PowerSample> samples;

	public int key() {
		return Breaker.intKey(panel, space);
	}

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

	public List<PowerSample> getSamples() {
		return samples;
	}

	public void setSamples(List<PowerSample> _samples) {
		samples = _samples;
	}
}
