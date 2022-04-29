package com.lanternsoftware.currentmonitor.adc;

public class MCP3008Pin {
	private final MCP3008 chip;
	private final int pin;

	public MCP3008Pin(MCP3008 _chip, int _pin) {
		chip = _chip;
		pin = _pin;
	}

	public int read() {
		return chip.readPin(pin);
	}
}
