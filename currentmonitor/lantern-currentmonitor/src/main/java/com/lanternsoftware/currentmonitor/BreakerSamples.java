package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.currentmonitor.adc.MCP3008Pin;
import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.datamodel.currentmonitor.hub.PowerSample;

import java.util.List;

public class BreakerSamples {
	private final Breaker breaker;
	private final MCP3008Pin voltagePin;
	private final MCP3008Pin currentPin;
	private final List<PowerSample> samples;
	private int cycleCnt;
	private int sampleCnt;

	public BreakerSamples(Breaker _breaker, MCP3008Pin _voltagePin, MCP3008Pin _currentPin, List<PowerSample> _samples) {
		breaker = _breaker;
		voltagePin = _voltagePin;
		currentPin = _currentPin;
		samples = _samples;
	}

	public Breaker getBreaker() {
		return breaker;
	}

	public MCP3008Pin getVoltagePin() {
		return voltagePin;
	}

	public MCP3008Pin getCurrentPin() {
		return currentPin;
	}

	public List<PowerSample> getSamples() {
		return samples;
	}

	public void incrementCycleCnt() {
		cycleCnt++;
	}

	public PowerSample incrementSample() {
		return samples.get(sampleCnt++);
	}

	public int getCycleCnt() {
		return cycleCnt;
	}

	public void setCycleCnt(int _cycleCnt) {
		cycleCnt = _cycleCnt;
	}

	public int getSampleCnt() {
		return sampleCnt;
	}

	public void setSampleCnt(int _sampleCnt) {
		sampleCnt = _sampleCnt;
	}
}
