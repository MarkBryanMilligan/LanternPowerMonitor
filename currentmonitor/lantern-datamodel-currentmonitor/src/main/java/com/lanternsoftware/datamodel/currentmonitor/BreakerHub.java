package com.lanternsoftware.datamodel.currentmonitor;


import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class BreakerHub {
	private int hub;
	private double voltageCalibrationFactor;
	private double portCalibrationFactor;
	private int frequency;
	private String bluetoothMac;

	public int getHub() {
		return hub;
	}

	public void setHub(int _hub) {
		hub = _hub;
	}

	public double getVoltageCalibrationFactor() {
		return voltageCalibrationFactor == 0.0?1.0:voltageCalibrationFactor;
	}

	public void setVoltageCalibrationFactor(double _voltageCalibrationFactor) {
		voltageCalibrationFactor = _voltageCalibrationFactor;
	}

	public double getPortCalibrationFactor() {
		return portCalibrationFactor == 0.0?1.0:portCalibrationFactor;
	}

	public void setPortCalibrationFactor(double _portCalibrationFactor) {
		portCalibrationFactor = _portCalibrationFactor;
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
