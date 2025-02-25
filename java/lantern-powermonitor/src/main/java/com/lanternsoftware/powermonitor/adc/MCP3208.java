package com.lanternsoftware.powermonitor.adc;

import com.lanternsoftware.pigpio.Spi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MCP3208 extends ADC {
	protected static final Logger LOG = LoggerFactory.getLogger(MCP3208.class);
	private static final byte[][] pins = new byte[8][];

	private final byte[] resp = new byte[3];

	static {
		for (int p = 0; p < 8; p++) {
			pins[p] = new byte[]{(byte)(6 | ((4 & p) >> 2)), (byte)((3 & p) << 6), 0};
		}
	}

	public MCP3208(Spi _spi, int _channel) {
		super(_spi, _channel);
	}

	public int readPin(int _pin) {
		if (spi != null && spi.transfer(pins[_pin], resp) > 2)
			return ((resp[1] & 0x0F) << 8) + (resp[2] & 0xFF);
		return 0;
	}
}
