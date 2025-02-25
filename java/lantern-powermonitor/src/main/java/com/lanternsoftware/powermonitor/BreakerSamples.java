package com.lanternsoftware.powermonitor;

import com.lanternsoftware.powermonitor.adc.ADCPort;
import com.lanternsoftware.powermonitor.datamodel.Breaker;
import com.lanternsoftware.powermonitor.datamodel.hub.PowerSample;

import java.util.List;

public class BreakerSamples {
	private final Breaker breaker;
	private final ADCPort port;
	private final List<PowerSample> samples;
	private int cycleCnt;
	private int sampleCnt;

	public BreakerSamples(Breaker _breaker, ADCPort _port, List<PowerSample> _samples) {
		breaker = _breaker;
		port = _port;
		samples = _samples;
	}

	public Breaker getBreaker() {
		return breaker;
	}

	public ADCPort getPort() {
		return port;
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
