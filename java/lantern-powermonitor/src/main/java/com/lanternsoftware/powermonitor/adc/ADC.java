package com.lanternsoftware.powermonitor.adc;

import com.lanternsoftware.pigpio.Spi;

public abstract class ADC {
	protected final Spi spi;
	protected final int channel;

	public ADC(Spi _spi, int _channel) {
		spi = _spi;
		channel = _channel;
	}

	public abstract int readPin(int _pin);

	public int getChannel() {
		return channel;
	}
}
