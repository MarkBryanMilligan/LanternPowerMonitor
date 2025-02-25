package com.lanternsoftware.powermonitor.pcb;

public class Port {
	private final int port;
	private final int spiChannel;
	private final int voltageChip;
	private final int voltagePin;
	private final int currentChip;
	private final int currentPin;

	public Port(int _port, int _spiChannel, int _voltageChip, int _voltagePin, int _currentChip, int _currentPin) {
		port = _port;
		spiChannel = _spiChannel;
		voltageChip = _voltageChip;
		voltagePin = _voltagePin;
		currentChip = _currentChip;
		currentPin = _currentPin;
	}

	public int getPort() {
		return port;
	}

	public int getSpiChannel() {
		return spiChannel;
	}

	public int getVoltageChip() {
		return voltageChip;
	}

	public int getVoltagePin() {
		return voltagePin;
	}

	public int getCurrentChip() {
		return currentChip;
	}

	public int getCurrentPin() {
		return currentPin;
	}
}
