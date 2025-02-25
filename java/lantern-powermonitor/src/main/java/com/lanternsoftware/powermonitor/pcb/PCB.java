package com.lanternsoftware.powermonitor.pcb;

import com.lanternsoftware.powermonitor.adc.ADC;
import com.lanternsoftware.powermonitor.adc.ADCPin;
import com.lanternsoftware.powermonitor.adc.ADCPort;
import com.lanternsoftware.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PCB {
	private static final Logger LOG = LoggerFactory.getLogger(PCB.class);
	protected static final Map<Integer, ADC> adcs = new HashMap<>();
	protected synchronized ADC getADC(int _spiChannel, int _chipSelect) {
		synchronized (adcs) {
			int chipId = (0xff & _chipSelect) | ((_spiChannel << 8) & 0xff00);
			ADC chip = adcs.get(chipId);
			if (chip == null) {
				LOG.info("Creating ADC on spi channel {} chip select {}", _spiChannel, _chipSelect);
				chip = createADC(_spiChannel, _chipSelect);
				adcs.put(chipId, chip);
			}
			return chip;
		}
	}
	protected final List<ADCPort> ports;

	public PCB(List<Port> _ports) {
		ports = CollectionUtils.transform(_ports, _p->new ADCPort(new ADCPin(getADC(_p.getSpiChannel(), _p.getVoltageChip()), _p.getVoltagePin()), new ADCPin(getADC(_p.getSpiChannel(), _p.getCurrentChip()), _p.getCurrentPin())));
	}

	protected abstract ADC createADC(int _spiChannel, int _chipSelect);
	public abstract int getVersion();
	public abstract double getCurrentCalibrationFactor();
	public abstract double getVoltageCalibrationFactor();

	public ADCPort getPort(int _currentPort) {
		return CollectionUtils.get(ports, _currentPort-1);
	}

	public int getSpiChannel(int _port) {
		ADCPort port = CollectionUtils.get(ports, _port);
		return port == null ? 0 : port.getChannel();
	}
}
