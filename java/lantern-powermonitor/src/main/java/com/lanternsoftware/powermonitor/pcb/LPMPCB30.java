package com.lanternsoftware.powermonitor.pcb;

import com.lanternsoftware.pigpio.PiGpioFactory;
import com.lanternsoftware.powermonitor.adc.ADC;
import com.lanternsoftware.powermonitor.adc.MCP3208;
import com.lanternsoftware.util.CollectionUtils;

public class LPMPCB30 extends PCB {
	public LPMPCB30() {
		super(CollectionUtils.asArrayList(
			new Port(1, 0, 0, 7, 1, 6),
			new Port(2, 0, 0, 7, 1, 4),
			new Port(3, 0, 0, 7, 1, 0),
			new Port(4, 1, 0, 7, 0, 3),
			new Port(5, 1, 0, 7, 1, 7),
			new Port(6, 1, 0, 7, 1, 4),
			new Port(7, 1, 0, 7, 1, 0),
			new Port(8, 0, 0, 7, 0, 4),
			new Port(9, 0, 0, 7, 0, 1),
			new Port(10, 0, 0, 7, 1, 5),
			new Port(11, 0, 0, 7, 1, 1),
			new Port(12, 1, 0, 7, 0, 4),
			new Port(13, 1, 0, 7, 0, 0),
			new Port(14, 1, 0, 7, 1, 3),
			new Port(15, 1, 0, 7, 1, 1),
			new Port(16, 0, 0, 7, 0, 5),
			new Port(17, 0, 0, 7, 0, 2),
			new Port(18, 0, 0, 7, 1, 7),
			new Port(19, 0, 0, 7, 1, 2),
			new Port(20, 1, 0, 7, 0, 5),
			new Port(21, 1, 0, 7, 0, 1),
			new Port(22, 1, 0, 7, 1, 5),
			new Port(23, 1, 0, 7, 1, 2),
			new Port(24, 0, 0, 7, 0, 6),
			new Port(25, 0, 0, 7, 0, 3),
			new Port(26, 0, 0, 7, 0, 0),
			new Port(27, 0, 0, 7, 1, 3),
			new Port(28, 1, 0, 7, 0, 6),
			new Port(29, 1, 0, 7, 0, 2),
			new Port(30, 1, 0, 7, 1, 6)
		));
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public double getCurrentCalibrationFactor() {
		return 0.19254;
	}

	@Override
	public double getVoltageCalibrationFactor() {
		return 0.26975;
	}

	@Override
	protected ADC createADC(int _spiChannel, int _chipSelect) {
		return new MCP3208(PiGpioFactory.getSpiChannel(_chipSelect, 1250000, _spiChannel > 0), _spiChannel);
	}
}
