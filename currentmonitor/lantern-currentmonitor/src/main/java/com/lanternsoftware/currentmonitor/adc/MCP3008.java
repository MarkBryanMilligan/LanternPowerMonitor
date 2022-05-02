package com.lanternsoftware.currentmonitor.adc;

import com.lanternsoftware.pigpio.Spi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MCP3008 {
	protected static final Logger LOG = LoggerFactory.getLogger(MCP3008.class);
	private static final byte[][] pins = new byte[8][];

	private final Spi spi;
	private final byte[] resp = new byte[3];

	static {
		for (int p = 0; p < 8; p++) {
			pins[p] = new byte[]{1,(byte)(p + 8 << 4),0};
		}
	}

	public MCP3008(Spi _spi) {
		spi = _spi;
	}

	public int readPin(int _pin) {
		if (spi != null && spi.transfer(pins[_pin], resp) > 2)
			return ((resp[1] & 0x03) << 8) + (resp[2] & 0xFF);
		return 0;
	}
}
