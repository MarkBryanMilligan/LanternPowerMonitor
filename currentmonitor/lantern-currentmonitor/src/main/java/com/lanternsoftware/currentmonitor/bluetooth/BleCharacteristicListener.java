package com.lanternsoftware.currentmonitor.bluetooth;

public interface BleCharacteristicListener {
	void write(String _name, byte[] _value);
	byte[] read(String _name);
}
