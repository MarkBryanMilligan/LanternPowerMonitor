package com.lanternsoftware.powermonitor;

import com.lanternsoftware.pigpio.PIGPIO;
import com.lanternsoftware.pigpio.PiGpioFactory;
import com.lanternsoftware.powermonitor.adc.ADCPort;
import com.lanternsoftware.powermonitor.datamodel.Breaker;
import com.lanternsoftware.powermonitor.datamodel.BreakerHub;
import com.lanternsoftware.powermonitor.datamodel.BreakerPolarity;
import com.lanternsoftware.powermonitor.datamodel.BreakerPower;
import com.lanternsoftware.powermonitor.datamodel.hub.BreakerSample;
import com.lanternsoftware.powermonitor.datamodel.hub.HubSample;
import com.lanternsoftware.powermonitor.datamodel.hub.PowerSample;
import com.lanternsoftware.powermonitor.pcb.LPMPCB1;
import com.lanternsoftware.powermonitor.pcb.LPMPCB30;
import com.lanternsoftware.powermonitor.pcb.PCB;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.MessageUtil;
import com.lanternsoftware.util.concurrency.ConcurrencyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PowerMonitor {
	private static final Logger LOG = LoggerFactory.getLogger(PowerMonitor.class);
	private static final int BATCH_CNT = 4;
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final PCB pcb = getPCB();
	private List<Sampler> samplers;
	private PowerListener listener;
	private boolean debug = false;
	private boolean postSamples = false;

	public static PCB getPCB() {
		if (PiGpioFactory.ensureInitialized()) {
			PIGPIO.gpioSetMode(3, 0); //INPUT
			PIGPIO.gpioSetPullUpDown(3, 2); //PULL UP
			if (PIGPIO.gpioRead(3) > 0)
				return new LPMPCB1();
		}
		return new LPMPCB30();
	}

	public void setDebug(boolean _debug) {
		debug = _debug;
	}

	public void setPostSamples(boolean _postSamples) {
		postSamples = _postSamples;
	}

	public void stop() {
		stopMonitoring();
		ConcurrencyUtils.sleep(1000);
		executor.shutdown();
		ConcurrencyUtils.sleep(1000);
		LOG.info("Power Monitor Service Stopped");
	}

	public CalibrationResult calibrateVoltage(double _curCalibration) {
		LOG.info("Calibrating Voltage");
		ADCPort port = pcb.getPort(1);
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
			samples[offset].voltage = port.readVoltage();
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
		LOG.info("Detected Frequency: {}", frequency);

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
			//TODO: Identify board, filter breakers for board
			List<Breaker> validBreakers = CollectionUtils.filter(_breakers, _b -> _b.getPort() > 0 && _b.getPort() < 31);
			if (CollectionUtils.isEmpty(validBreakers)) {
				LOG.error("No breakers found for hub number {}", _hub.getHub());
				return;
			}
			LOG.info("Board Version: {}", pcb.getVersion());
			LOG.info("Monitoring {} breakers for hub {}", CollectionUtils.size(validBreakers), _hub.getHub());
			Map<Integer, List<Breaker>> spiBreakers = CollectionUtils.transformToMultiMap(validBreakers, _b->pcb.getSpiChannel(_b.getPort()));
			LOG.info("Starting to monitor ports {}\nUsing {} sampler threads.", CollectionUtils.transformToCommaSeparated(validBreakers, _b -> String.valueOf(_b.getPort())), spiBreakers.size());
			samplers = CollectionUtils.transform(spiBreakers.values(), _b->new Sampler(_hub, _b, _intervalMs, 5));
			samplers.forEach(executor::submit);
		}
		catch (Throwable t) {
			LOG.error("throwable", t);
		}
	}

	public void submit(Runnable _runnable) {
		executor.submit(_runnable);
	}

	public void stopMonitoring() {
		CollectionUtils.makeNotNull(samplers).forEach(Sampler::stop);
		samplers = null;
	}

	private class Sampler implements Runnable {
		private boolean running = true;
		private final BreakerHub hub;
		private final List<List<BreakerSamples>> breakers;
		private final long intervalNs;
		private final int concurrentBreakerCnt;

		public Sampler(BreakerHub _hub, List<Breaker> _breakers, long _intervalMs, int _concurrentBreakerCnt) {
			hub = _hub;
			breakers = CollectionUtils.transform(_breakers, _b->{
				ADCPort port = pcb.getPort(_b.getPort());
				if (port == null) {
					LOG.info("Could not get current pin for port {} due to board/config mismatch", _b.getPort());
					return null;
				}
				List<BreakerSamples> batches = new ArrayList<>(BATCH_CNT);
				for (int i=0; i<BATCH_CNT; i++) {
					List<PowerSample> samples = new ArrayList<>(60000/_breakers.size());
					for (int j=0; j<60000/_breakers.size(); j++) {
						samples.add(new PowerSample());
					}
					batches.add(new BreakerSamples(_b, port, samples));
				}
				return batches;
			}, true);
			intervalNs = _intervalMs*1000000;
			concurrentBreakerCnt = Math.min(_breakers.size(), _concurrentBreakerCnt);
		}

		@Override
		public void run() {
			long start = System.nanoTime();
			long interval = 0;
			int cycle;
			int curBreaker;
			long intervalStart;
			long intervalEnd;
			long cycleEnd;
			long curTime;
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
					intervalStart = (interval * intervalNs) + start;
					intervalEnd = intervalStart + intervalNs;
					cycle = 0;
					final int batch = (int) (interval % BATCH_CNT);
					while (System.nanoTime() < intervalEnd) {
						for (curBreaker = 0; curBreaker < concurrentBreakerCnt; curBreaker++) {
							cycleBreakers[curBreaker] = breakers.get(((cycle * concurrentBreakerCnt) + curBreaker) % breakers.size()).get(batch);
							cycleBreakers[curBreaker].incrementCycleCnt();
						}
						cycle++;
						cycleEnd = intervalStart + (cycle * (intervalNs / hub.getFrequency()));
						curTime = System.nanoTime();
						while (curTime < cycleEnd) {
							for (curBreaker = 0; curBreaker < concurrentBreakerCnt; curBreaker++) {
								ADCPort port = cycleBreakers[curBreaker].getPort();
								PowerSample sample = cycleBreakers[curBreaker].incrementSample();
								sample.nanoTime = curTime;
								sample.cycle = cycle;
								sample.voltage = port.readVoltage();
								sample.current = port.readCurrent();
							}
							curTime = System.nanoTime();
						}
					}
					interval++;
					final HubSample hubSample = (postSamples && ((interval == 10) || (interval == 100))) ? new HubSample() : null;
					executor.submit(() -> {
						long cycleLength = 1000000000/hub.getFrequency();
						if (hubSample != null) {
							hubSample.setSampleDate(new Date());
							hubSample.setBreakers(new ArrayList<>());
						}
						for (List<BreakerSamples> breaker : breakers) {
							BreakerSamples samples = breaker.get(batch);
							List<PowerSample> validSamples = samples.getSamples().subList(0, samples.getSampleCnt());
							BreakerSample breakerSample = null;
							if (hubSample != null) {
								breakerSample = new BreakerSample();
								breakerSample.setSamples(CollectionUtils.transform(validSamples, _s->new PowerSample(_s.nanoTime, _s.cycle, _s.voltage, _s.current)));
								breakerSample.setPanel(samples.getBreaker().getPanel());
								breakerSample.setSpace(samples.getBreaker().getSpace());
								hubSample.getBreakers().add(breakerSample);
							}
							int phaseOffsetNs = samples.getBreaker().getPhaseOffsetNs()-hub.getPhaseOffsetNs();
							if (phaseOffsetNs != 0) {
								Map<Integer, List<PowerSample>> cycles = CollectionUtils.transformToMultiMap(validSamples, _p->_p.cycle);
								for (List<PowerSample> cycleSamples : cycles.values()) {
									long minNano;
									long maxNano = minNano = cycleSamples.get(0).nanoTime;
									for (PowerSample sample : cycleSamples) {
										if (sample.nanoTime < minNano)
											minNano = sample.nanoTime;
										if (sample.nanoTime > maxNano)
											maxNano = sample.nanoTime;
									}
									TreeMap<Long, Double> offsetSamples = new TreeMap<>();
									for (PowerSample sample : cycleSamples) {
										if (sample.nanoTime + phaseOffsetNs < minNano)
											offsetSamples.put(sample.nanoTime + phaseOffsetNs + cycleLength, sample.voltage);
										else if (sample.nanoTime + phaseOffsetNs > maxNano)
											offsetSamples.put(sample.nanoTime + phaseOffsetNs - cycleLength, sample.voltage);
										else
											offsetSamples.put(sample.nanoTime + phaseOffsetNs, sample.voltage);
									}
									for (PowerSample sample : cycleSamples) {
										List<Double> voltages = new ArrayList<>();
										Entry<Long, Double> floorEntry = offsetSamples.floorEntry(sample.nanoTime);
										if (floorEntry != null)
											voltages.add(floorEntry.getValue());
										Entry<Long, Double> ceilingEntry = offsetSamples.ceilingEntry(sample.nanoTime);
										if (ceilingEntry != null)
											voltages.add(ceilingEntry.getValue());
										sample.voltage = CollectionUtils.mean(voltages);
									}
								}
							}

							double vOffset = 0.0;
							double iOffset = 0.0;
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
							vRms = pcb.getVoltageCalibrationFactor() * hub.getVoltageCalibrationFactor() * Math.sqrt(vRms);
							int lowSampleRatio = (lowSamples * 100) / validSamples.size();
							double realPower = (pcb.getVoltageCalibrationFactor() * hub.getVoltageCalibrationFactor() * pcb.getCurrentCalibrationFactor() * hub.getPortCalibrationFactor() * samples.getBreaker().getCalibrationFactor() * pSum) / validSamples.size();
							if ((lowSampleRatio > 75) && Math.abs(realPower) < 13.0)
								realPower = 0.0;
							if (samples.getBreaker().getPolarity() == BreakerPolarity.NORMAL)
								realPower = Math.abs(realPower);
							else if (samples.getBreaker().getPolarity() == BreakerPolarity.SOLAR)
								realPower = -Math.abs(realPower);
							else if (samples.getBreaker().getPolarity() == BreakerPolarity.BI_DIRECTIONAL_INVERTED)
								realPower = -realPower;
							if (samples.getBreaker().isDoublePower())
								realPower *= 2.0;
							if (debug || (breakerSample != null)) {
								synchronized (PowerMonitor.this) {
									StringBuilder log = new StringBuilder();
									log.append(MessageUtil.msg("===========================Start Port {}", samples.getBreaker().getPort()));
									log.append("\n");
									log.append(MessageUtil.msg("Cycles: {}", samples.getCycleCnt()));
									log.append("\n");
									log.append(MessageUtil.msg("Samples: {}", samples.getSampleCnt()));
									log.append("\n");
									log.append(MessageUtil.msg("vMin: {}, vMax: {}, vOffset: {}", String.format("%.3f", CollectionUtils.getSmallest(validSamples, Comparator.comparing(_v -> _v.voltage)).voltage), String.format("%.3f", CollectionUtils.getLargest(validSamples, Comparator.comparing(_v -> _v.voltage)).voltage), String.format("%.3f", vOffset)));
									log.append("\n");
									log.append(MessageUtil.msg("iMin: {}, iMax: {}, iOffset: {}", String.format("%.3f", CollectionUtils.getSmallest(validSamples, Comparator.comparing(_v -> _v.current)).current), String.format("%.3f", CollectionUtils.getLargest(validSamples, Comparator.comparing(_v -> _v.current)).current), String.format("%.3f", iOffset)));
									log.append("\n");
									double iRms = pcb.getCurrentCalibrationFactor() * hub.getPortCalibrationFactor() * samples.getBreaker().getCalibrationFactor() * Math.sqrt(CollectionUtils.mean(CollectionUtils.transform(validSamples, _p -> _p.current * _p.current)));
									log.append(MessageUtil.msg("vRms: {}", String.format("%.3f", vRms)));
									log.append("\n");
									log.append(MessageUtil.msg("iRms: {}", String.format("%.3f", iRms)));
									log.append("\n");
									double apparentPower = vRms * iRms;
									log.append(MessageUtil.msg("Apparent Power: {} watts", String.format("%.3f", apparentPower)));
									log.append("\n");
									log.append(MessageUtil.msg("Real Power: {} watts", String.format("%.3f", realPower)));
									log.append("\n");
									double powerFactor = realPower / apparentPower;
									log.append(MessageUtil.msg("Power Factor: {}", String.format("%.3f", powerFactor)));
									log.append("\n");
									log.append(MessageUtil.msg("===========================End Port {}", samples.getBreaker().getPort()));
									log.append("\n");
									String l = log.toString();
									LOG.info(l);
									if (breakerSample != null)
										breakerSample.setLog(l);
								}
							}
							samples.setSampleCnt(0);
							samples.setCycleCnt(0);
							if (breakerSample != null) {
								breakerSample.setCalculatedVoltage(vRms);
								breakerSample.setCalculatedPower(realPower);
							}
							listener.onPowerEvent(new BreakerPower(samples.getBreaker().getPanel(), samples.getBreaker().getSpace(), readTime, realPower, vRms));
						}
						if (hubSample != null)
							listener.onSampleEvent(hubSample);
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
