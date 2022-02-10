package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.datamodel.currentmonitor.Breaker;
import com.lanternsoftware.datamodel.currentmonitor.BreakerHub;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPolarity;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPower;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.concurrency.ConcurrencyUtils;
import com.pi4j.gpio.extension.base.AdcGpioProvider;
import com.pi4j.gpio.extension.mcp.MCP3008GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP3008Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
	private GpioController gpio;
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final Map<Integer, AdcGpioProvider> chips = new HashMap<>();
	private final Map<Integer, GpioPinAnalogInput> pins = new HashMap<>();
	private Sampler sampler;
	private PowerListener listener;
	private boolean debug = false;

	public void start() {
		try {
			gpio = GpioFactory.getInstance();
			LOG.info("Current Monitor Started");
		}
		catch (Throwable t) {
			LOG.info("Failed to get gpio factory", t);
		}
	}

	public void stop() {
		stopMonitoring();
		ConcurrencyUtils.sleep(1000);
		executor.shutdown();
		ConcurrencyUtils.sleep(1000);
		chips.clear();
		pins.clear();
		gpio.shutdown();
		LOG.info("Power Monitor Service Stopped");
	}

	public void setDebug(boolean _debug) {
		debug = _debug;
	}

	public CalibrationResult calibrateVoltage(double _curCalibration) {
		GpioPinAnalogInput voltagePin = getPin(0, 0);
		if (voltagePin == null)
			return null;
		int maxSamples = 120000;
		CalibrationSample[] samples = new CalibrationSample[maxSamples];
		int offset = 0;
		for (;offset < maxSamples; offset++) {
			samples[offset] = new CalibrationSample();
		}
		offset = 0;
		long intervalEnd = System.nanoTime() + 2000000000L; //Scan voltage for 2 seconds
		while (offset < maxSamples) {
			samples[offset].time = System.nanoTime();
			samples[offset].voltage = voltagePin.getValue();
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
			if (CollectionUtils.isEmpty(validBreakers))
				return;
			LOG.info("Monitoring {} breakers for hub {}", CollectionUtils.size(validBreakers), _hub.getHub());
			sampler = new Sampler(_hub, validBreakers, _intervalMs, 2);
			LOG.info("Starting to monitor ports {}", CollectionUtils.transformToCommaSeparated(validBreakers, _b -> String.valueOf(_b.getPort())));
			executor.submit(sampler);
		}
		catch (Throwable t) {
			LOG.error("throwable", t);
		}
	}

	private GpioPinAnalogInput getPin(int _chip, int _pin) {
		GpioPinAnalogInput pin;
		synchronized (pins) {
			AdcGpioProvider chip = chips.get(_chip);
			if (chip == null) {
				SpiChannel channel = SpiChannel.getByNumber(_chip);
				if (channel == null)
					return null;
				try {
					chip = new MCP3008GpioProvider(channel, 1250000, SpiDevice.DEFAULT_SPI_MODE, false);
					chips.put(_chip, chip);
				} catch (IOException _e) {
					LOG.error("Failed to connect to chip {}", _chip, _e);
					return null;
				}
			}
			int pinKey = pinKey(_chip, _pin);
			pin = pins.get(pinKey);
			if (pin == null) {
				pin = gpio.provisionAnalogInputPin(chip, MCP3008Pin.ALL[_pin], String.valueOf(pinKey));
				pins.put(pinKey, pin);
			}
		}
		return pin;
	}

	private Integer pinKey(int _chip, int _pin) {
		return (_chip*8)+_pin;
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
			GpioPinAnalogInput voltagePin = getPin(0, 0);
			breakers = CollectionUtils.transform(_breakers, _b->{
				LOG.info("Getting Chip {}, Pin {} for port {}", _b.getChip(), _b.getPin(), _b.getPort());
				GpioPinAnalogInput currentPin = getPin(_b.getChip(), _b.getPin());
				List<BreakerSamples> batches = new ArrayList<>(BATCH_CNT);
				for (int i=0; i<BATCH_CNT; i++) {
					List<PowerSample> samples = new ArrayList<>(30000/_breakers.size());
					for (int j=0; j<30000/_breakers.size(); j++) {
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
					int curBreaker;
					for (curBreaker = 0; curBreaker < breakers.size(); curBreaker++) {
						breakers.get(curBreaker).get(batch).setSampleCnt(0);
					}
					while (System.nanoTime() < intervalEnd) {
						for (curBreaker = 0; curBreaker < concurrentBreakerCnt; curBreaker++) {
							cycleBreakers[curBreaker] = breakers.get(((cycle * concurrentBreakerCnt) + curBreaker) % breakers.size()).get(batch);
						}
						cycle++;
						long cycleEnd = intervalStart + (cycle * (intervalNs / hub.getFrequency()));
						while (System.nanoTime() < cycleEnd) {
							for (curBreaker = 0; curBreaker < concurrentBreakerCnt; curBreaker++) {
								PowerSample sample = cycleBreakers[curBreaker].getSample(cycleBreakers[curBreaker].getSampleCnt());
								sample.voltage = cycleBreakers[curBreaker].getVoltagePin().getValue();
								sample.current = cycleBreakers[curBreaker].getCurrentPin().getValue();
								cycleBreakers[curBreaker].setSampleCnt(cycleBreakers[curBreaker].getSampleCnt()+1);
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
									LOG.info("Samples: {}", samples.getSampleCnt());
									LOG.info("vMin: {}, vMax: {}, vOffset: {}", String.format("%.3f", CollectionUtils.getSmallest(validSamples, Comparator.comparing(_v -> _v.voltage)).voltage), String.format("%.3f", CollectionUtils.getLargest(validSamples, Comparator.comparing(_v -> _v.voltage)).voltage), String.format("%.3f", vOffset));
									LOG.info("iMin: {}, iMax: {}, iOffset: {}", String.format("%.3f", CollectionUtils.getSmallest(validSamples, Comparator.comparing(_v -> _v.current)).current), String.format("%.3f", CollectionUtils.getLargest(validSamples, Comparator.comparing(_v -> _v.current)).current), String.format("%.3f", iOffset));
									double iRms = samples.getBreaker().getFinalCalibrationFactor() * Math.sqrt(CollectionUtils.mean(CollectionUtils.transform(validSamples, _p -> _p.current * _p.current)));
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
