package com.lanternsoftware.datamodel.currentmonitor;


import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class BreakerHub {
	private int hub;
	private double voltageCalibrationFactor;
	private int frequency;
	private String bluetoothMac;

	public int getHub() {
		return hub;
	}

	public void setHub(int _hub) {
		hub = _hub;
	}

	public double getVoltageCalibrationFactor() {
		return voltageCalibrationFactor;
	}

	public void setVoltageCalibrationFactor(double _voltageCalibrationFactor) {
		voltageCalibrationFactor = _voltageCalibrationFactor;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int _frequency) {
		frequency = _frequency;
	}

	public String getBluetoothMac() {
		return bluetoothMac;
	}

	public void setBluetoothMac(String _bluetoothMac) {
		bluetoothMac = _bluetoothMac;
	}
}
