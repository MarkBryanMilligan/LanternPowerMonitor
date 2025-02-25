package com.lanternsoftware.powermonitor.adc;

public class ADCPin {
	private final ADC adc;
	private final int pin;

	public ADCPin(ADC _adc, int _pin) {
		adc = _adc;
		pin = _pin;
	}

	public int read() {
		return adc.readPin(pin);
	}

	public int getChannel() {
		return adc.getChannel();
	}
}
