package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.currentmonitor.adc.MCP3008;
import com.lanternsoftware.currentmonitor.adc.MCP3008Pin;
import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.datamodel.currentmonitor.BreakerHub;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPolarity;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPower;
import com.lanternsoftware.pigpio.PiGpioFactory;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.concurrency.ConcurrencyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CurrentMonitor {
	private static final Logger LOG = LoggerFactory.getLogger(CurrentMonitor.class);
	private static final int BATCH_CNT = 4;
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final Map<Integer, MCP3008> chips = new HashMap<>();
	private Sampler sampler;
	private PowerListener listener;
	private boolean debug = false;

	public void stop() {
		stopMonitoring();
		ConcurrencyUtils.sleep(1000);
		executor.shutdown();
		ConcurrencyUtils.sleep(1000);
		PiGpioFactory.shutdown();
		chips.clear();
		LOG.info("Power Monitor Service Stopped");
	}

	public void setDebug(boolean _debug) {
		debug = _debug;
	}

	public CalibrationResult calibrateVoltage(double _curCalibration) {
		LOG.info("Calibrating Voltage");
		MCP3008Pin voltagePin = new MCP3008Pin(getChip(0), 0);
		int maxSamples = 240000;
		CalibrationSample[] samples = new CalibrationSample[maxSamples];
		int offset = 0;
		for (;offset < maxSamples; offset++) {
			samples[offset] = new CalibrationSample();
		}
		offset = 0;
		long intervalEnd = System.nanoTime() + 2000000000L; //Scan voltage for 2 seconds
		while (offset < maxSamples) {
			samples[offset].time = System.nanoTime();
			samples[offset].voltage = voltagePin.read();
			offset++;
			if (samples[offset-1].time > intervalEnd)
				break;
		}
		double vOffset = 0.0;
		for (CalibrationSample sample : samples) {
			vOffset += sample.voltage;
		}
		vOffset /= offset;
		int cycles = 0;
		boolean under = true;
		if (samples[0].voltage > (vOffset * 1.3)) {
			cycles = 1;
			under = false;
		}
		double voltage;
		double vRms = 0.0;
		for (int sample = 0; sample < offset; sample++) {
			voltage = samples[sample].voltage - vOffset;
			vRms += voltage * voltage;
			if (under && (samples[sample].voltage > (vOffset * 1.3))) {
				cycles += 1;
				under = false;
			}
			else if (samples[sample].voltage < vOffset * 0.7) {
				under = true;
			}
		}
		vRms /= offset;

		double oldVrms = _curCalibration * Math.sqrt(vRms);
		if (oldVrms < 20) {
			LOG.error("Could not get a valid voltage read, please check that your AC/AC transformer is connected");
			return null;
		}
		int frequency = Math.round(cycles/((samples[offset-1].time-samples[0].time)/100000000f))*10;
		LOG.info("Detected Frequency: " + frequency);

		double newCal = ((frequency > 55 ? 120:230)/oldVrms) * _curCalibration;
		double newVrms = newCal * Math.sqrt(vRms);
		LOG.info("Old Voltage Calibration: {}  Old vRMS: {}", _curCalibration, oldVrms);
		LOG.info("New Voltage Calibration: {}  New vRMS: {}", newCal, newVrms);
		return new CalibrationResult(newCal, frequency);
	}

	public void monitorPower(BreakerHub _hub, List<Breaker> _breakers, int _intervalMs, PowerListener _listener) {
		try {
			stopMonitoring();
			listener = _listener;
			List<Breaker> validBreakers = CollectionUtils.filter(_breakers, _b -> _b.getPort() > 0 && _b.getPort() < 16);
			if (CollectionUtils.isEmpty(validBreakers)) {
				LOG.error("No breakers found for hub number {}", _hub.getHub());
				return;
			}
			LOG.info("Monitoring {} breakers for hub {}", CollectionUtils.size(validBreakers), _hub.getHub());
			sampler = new Sampler(_hub, validBreakers, _intervalMs, 5);
			LOG.info("Starting to monitor ports {}", CollectionUtils.transformToCommaSeparated(validBreakers, _b -> String.valueOf(_b.getPort())));
			executor.submit(sampler);
		}
		catch (Throwable t) {
			LOG.error("throwable", t);
		}
	}

	private synchronized MCP3008 getChip(int _chip) {
		MCP3008 chip = chips.get(_chip);
		if (chip == null) {
			String id = "SPI" + _chip;
			LOG.info("Creating chip {}", id);
			chip = new MCP3008(PiGpioFactory.getSpiChannel(_chip, 810000, false));
			chips.put(_chip, chip);
		}
		return chip;
	}

	public void submit(Runnable _runnable) {
		executor.submit(_runnable);
	}

	public void stopMonitoring() {
		if (sampler != null) {
			sampler.stop();
			sampler = null;
		}
	}

	private class Sampler implements Runnable {
		private boolean running = true;
		private final BreakerHub hub;
		private final List<List<BreakerSamples>> breakers;
		private final int intervalNs;
		private final int concurrentBreakerCnt;

		public Sampler(BreakerHub _hub, List<Breaker> _breakers, int _intervalMs, int _concurrentBreakerCnt) {
			hub = _hub;
			MCP3008Pin voltagePin = new MCP3008Pin(getChip(0), 0);
			breakers = CollectionUtils.transform(_breakers, _b->{
				LOG.info("Getting Chip {}, Pin {} for port {}", _b.getChip(), _b.getPin(), _b.getPort());
				MCP3008Pin currentPin = new MCP3008Pin(getChip(_b.getChip()), _b.getPin());
				List<BreakerSamples> batches = new ArrayList<>(BATCH_CNT);
				for (int i=0; i<BATCH_CNT; i++) {
					List<PowerSample> samples = new ArrayList<>(30000/_breakers.size());
					for (int j=0; j<60000/_breakers.size(); j++) {
						samples.add(new PowerSample());
					}
					batches.add(new BreakerSamples(_b, voltagePin, currentPin, samples));
				}
				return batches;
			});
			intervalNs = _intervalMs*1000000;
			concurrentBreakerCnt = Math.min(_breakers.size(), _concurrentBreakerCnt);
		}

		@Override
		public void run() {
			long start = System.nanoTime();
			long interval = 0;
			int cycle;
			int curBreaker;
			BreakerSamples[] cycleBreakers = new BreakerSamples[concurrentBreakerCnt];
			try {
				while (true) {
					synchronized (this) {
						if (!running) {
							LOG.info("Power Monitoring Stopped");
							break;
						}
					}
					final Date readTime = new Date();
					final long intervalStart = (interval * intervalNs) + start;
					long intervalEnd = intervalStart + intervalNs;
					cycle = 0;
					final int batch = (int) (interval % BATCH_CNT);
					while (System.nanoTime() < intervalEnd) {
						for (curBreaker = 0; curBreaker < concurrentBreakerCnt; curBreaker++) {
							cycleBreakers[curBreaker] = breakers.get(((cycle * concurrentBreakerCnt) + curBreaker) % breakers.size()).get(batch);
							cycleBreakers[curBreaker].incrementCycleCnt();
						}
						cycle++;
						long cycleEnd = intervalStart + (cycle * (intervalNs / hub.getFrequency()));
						while (System.nanoTime() < cycleEnd) {
							for (curBreaker = 0; curBreaker < concurrentBreakerCnt; curBreaker++) {
								PowerSample sample = cycleBreakers[curBreaker].incrementSample();
								sample.voltage = cycleBreakers[curBreaker].getVoltagePin().read();
								sample.current = cycleBreakers[curBreaker].getCurrentPin().read();
							}
						}
					}
					interval++;
					executor.submit(() -> {
						for (List<BreakerSamples> breaker : breakers) {
							double vOffset = 0.0;
							double iOffset = 0.0;
							BreakerSamples samples = breaker.get(batch);
							List<PowerSample> validSamples = samples.getSamples().subList(0, samples.getSampleCnt());
							for (PowerSample sample : validSamples) {
								vOffset += sample.voltage;
								iOffset += sample.current;
							}
							vOffset /= samples.getSampleCnt();
							iOffset /= samples.getSampleCnt();
							int lowSamples = 0;
							double pSum = 0.0;
							double vRms = 0.0;
							double lowPassFilter = samples.getBreaker().getLowPassFilter();
							for (PowerSample sample : validSamples) {
								sample.current -= iOffset;
								if (Math.abs(sample.current) < lowPassFilter)
									lowSamples++;
								sample.voltage -= vOffset;
								pSum += sample.current * sample.voltage;
								vRms += sample.voltage * sample.voltage;
							}
							vRms /= validSamples.size();
							vRms = hub.getVoltageCalibrationFactor() * Math.sqrt(vRms);
							int lowSampleRatio = (lowSamples * 100) / samples.getSampleCnt();
							double realPower = Math.abs((hub.getVoltageCalibrationFactor() * hub.getPortCalibrationFactor() * samples.getBreaker().getFinalCalibrationFactor() * pSum) / samples.getSampleCnt());
							if ((lowSampleRatio > 75) && realPower < 13.0)
								realPower = 0.0;
							if (samples.getBreaker().getPolarity() == BreakerPolarity.SOLAR)
								realPower = -realPower;
							if (samples.getBreaker().isDoublePower())
								realPower *= 2.0;
							if (debug) {
								synchronized (CurrentMonitor.this) {
									LOG.info("===========================Start Port {}", samples.getBreaker().getPort());
									LOG.info("Cycles: {}", samples.getCycleCnt());
									LOG.info("Samples: {}", samples.getSampleCnt());
									LOG.info("vMin: {}, vMax: {}, vOffset: {}", String.format("%.3f", CollectionUtils.getSmallest(validSamples, Comparator.comparing(_v -> _v.voltage)).voltage), String.format("%.3f", CollectionUtils.getLargest(validSamples, Comparator.comparing(_v -> _v.voltage)).voltage), String.format("%.3f", vOffset));
									LOG.info("iMin: {}, iMax: {}, iOffset: {}", String.format("%.3f", CollectionUtils.getSmallest(validSamples, Comparator.comparing(_v -> _v.current)).current), String.format("%.3f", CollectionUtils.getLargest(validSamples, Comparator.comparing(_v -> _v.current)).current), String.format("%.3f", iOffset));
									double iRms = hub.getPortCalibrationFactor() * samples.getBreaker().getFinalCalibrationFactor() * Math.sqrt(CollectionUtils.mean(CollectionUtils.transform(validSamples, _p -> _p.current * _p.current)));
									LOG.info("vRms: {}", String.format("%.3f", vRms));
									LOG.info("iRms: {}", String.format("%.3f", iRms));
									double apparentPower = vRms * iRms;
									LOG.info("Apparent Power: {} watts", String.format("%.3f", apparentPower));
									LOG.info("Real Power: {} watts", String.format("%.3f", realPower));
									double powerFactor = realPower / apparentPower;
									LOG.info("Power Factor: {}", String.format("%.3f", powerFactor));
									LOG.info("===========================End Port {}", samples.getBreaker().getPort());
								}
							}
							samples.setSampleCnt(0);
							samples.setCycleCnt(0);
							listener.onPowerEvent(new BreakerPower(samples.getBreaker().getPanel(), samples.getBreaker().getSpace(), readTime, realPower, vRms));
						}
					});
				}
			}
			catch (Throwable t) {
				LOG.error("Exception while monitoring power", t);
			}
		}

		synchronized void stop() {
			running = false;
		}
	}
}
