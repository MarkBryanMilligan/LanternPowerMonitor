package com.lanternsoftware.powermonitor.datamodel.archive;

import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class BreakerEnergyArchive {
	private int panel;
	private int space;
	private byte[] readings;

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

	public byte[] getReadings() {
		return readings;
	}

	public void setReadings(byte[] _readings) {
		readings = _readings;
	}
}
