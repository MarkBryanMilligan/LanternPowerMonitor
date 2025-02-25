package com.lanternsoftware.powermonitor.adc;

public class ADCPort {
	private final ADCPin voltagePin;
	private final ADCPin currentPin;

	public ADCPort(ADCPin _voltagePin, ADCPin _currentPin) {
		voltagePin = _voltagePin;
		currentPin = _currentPin;
	}

	public int readVoltage() {
		return voltagePin.read();
	}

	public int readCurrent() {
		return currentPin.read();
	}

	public int getChannel() {
		return voltagePin.getChannel();
	}
}
