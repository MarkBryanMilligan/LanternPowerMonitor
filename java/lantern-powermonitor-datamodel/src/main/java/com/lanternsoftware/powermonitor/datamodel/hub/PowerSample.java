package com.lanternsoftware.powermonitor.datamodel.hub;

import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class PowerSample {
	public long nanoTime;
	public int cycle;
	public double voltage;
	public double current;

	public PowerSample() {
	}

	public PowerSample(long _nanoTime, int _cycle, double _voltage, double _current) {
		nanoTime = _nanoTime;
		cycle = _cycle;
		voltage = _voltage;
		current = _current;
	}

	public long getNanoTime() {
		return nanoTime;
	}

	public void setNanoTime(long _nanoTime) {
		nanoTime = _nanoTime;
	}

	public int getCycle() {
		return cycle;
	}

	public void setCycle(int _cycle) {
		cycle = _cycle;
	}

	public double getVoltage() {
		return voltage;
	}

	public void setVoltage(double _voltage) {
		voltage = _voltage;
	}

	public double getCurrent() {
		return current;
	}

	public void setCurrent(double _current) {
		current = _current;
	}
}
