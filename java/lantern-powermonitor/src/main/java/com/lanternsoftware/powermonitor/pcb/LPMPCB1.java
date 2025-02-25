package com.lanternsoftware.powermonitor.pcb;

import com.lanternsoftware.pigpio.PiGpioFactory;
import com.lanternsoftware.powermonitor.adc.ADC;
import com.lanternsoftware.powermonitor.adc.MCP3008;
import com.lanternsoftware.util.CollectionUtils;

public class LPMPCB1 extends PCB {
	public LPMPCB1() {
		super(CollectionUtils.asArrayList(
			new Port(1, 0, 0, 0, 1, 0),
			new Port(2, 0, 0, 0, 1, 1),
			new Port(3, 0, 0, 0, 1, 2),
			new Port(4, 0, 0, 0, 1, 3),
			new Port(5, 0, 0, 0, 1, 4),
			new Port(6, 0, 0, 0, 1, 5),
			new Port(7, 0, 0, 0, 1, 6),
			new Port(8, 0, 0, 0, 1, 7),
			new Port(9, 0, 0, 0, 0, 1),
			new Port(10, 0, 0, 0, 0, 2),
			new Port(11, 0, 0, 0, 0, 3),
			new Port(12, 0, 0, 0, 0, 4),
			new Port(13, 0, 0, 0, 0, 5),
			new Port(14, 0, 0, 0, 0, 6),
			new Port(15, 0, 0, 0, 0, 7)
		));
	}

	@Override
	public int getVersion() {
		return 0;
	}

	@Override
	public double getCurrentCalibrationFactor() {
		return 1.0;
	}

	@Override
	public double getVoltageCalibrationFactor() {
		return 1.0;
	}

	@Override
	protected ADC createADC(int _spiChannel, int _chipSelect) {
		return new MCP3008(PiGpioFactory.getSpiChannel(_chipSelect, 810000, _spiChannel > 0), _spiChannel);
	}
}
