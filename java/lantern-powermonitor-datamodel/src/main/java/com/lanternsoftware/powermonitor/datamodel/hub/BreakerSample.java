package com.lanternsoftware.powermonitor.datamodel.hub;

import com.lanternsoftware.powermonitor.datamodel.Breaker;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.List;

@DBSerializable
public class BreakerSample {
	private int panel;
	private int space;
	private double calculatedVoltage;
	private double calculatedPower;
	private String log;
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

	public double getCalculatedVoltage() {
		return calculatedVoltage;
	}

	public void setCalculatedVoltage(double _calculatedVoltage) {
		calculatedVoltage = _calculatedVoltage;
	}

	public double getCalculatedPower() {
		return calculatedPower;
	}

	public void setCalculatedPower(double _calculatedPower) {
		calculatedPower = _calculatedPower;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String _log) {
		log = _log;
	}

	public List<PowerSample> getSamples() {
		return samples;
	}

	public void setSamples(List<PowerSample> _samples) {
		samples = _samples;
	}
}
