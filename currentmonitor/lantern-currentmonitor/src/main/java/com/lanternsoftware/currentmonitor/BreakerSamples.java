package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.pi4j.io.gpio.GpioPinAnalogInput;

import java.util.List;

public class BreakerSamples {
	private final Breaker breaker;
	private final GpioPinAnalogInput voltagePin;
	private final GpioPinAnalogInput currentPin;
	private final List<PowerSample> samples;
	private int sampleCnt;

	public BreakerSamples(Breaker _breaker, GpioPinAnalogInput _voltagePin, GpioPinAnalogInput _currentPin, List<PowerSample> _samples) {
		breaker = _breaker;
		voltagePin = _voltagePin;
		currentPin = _currentPin;
		samples = _samples;
	}

	public Breaker getBreaker() {
		return breaker;
	}

	public GpioPinAnalogInput getVoltagePin() {
		return voltagePin;
	}

	public GpioPinAnalogInput getCurrentPin() {
		return currentPin;
	}

	public List<PowerSample> getSamples() {
		return samples;
	}

	public PowerSample getSample(int _sample) {
		return samples.get(_sample);
	}

	public int getSampleCnt() {
		return sampleCnt;
	}

	public void setSampleCnt(int _sampleCnt) {
		sampleCnt = _sampleCnt;
	}
}
