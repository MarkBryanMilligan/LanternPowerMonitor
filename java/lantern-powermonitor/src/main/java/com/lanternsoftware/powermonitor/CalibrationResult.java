package com.lanternsoftware.powermonitor;

public class CalibrationResult {
	private final double voltageCalibrationFactor;
	private final int frequency;

	public CalibrationResult(double _voltageCalibrationFactor, int _frequency) {
		voltageCalibrationFactor = _voltageCalibrationFactor;
		frequency = _frequency;
	}

	public double getVoltageCalibrationFactor() {
		return voltageCalibrationFactor;
	}

	public int getFrequency() {
		return frequency;
	}
}
