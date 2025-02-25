package com.lanternsoftware.pigpio;

public class Spi {
	private final int handle;

	public Spi(int _handle) {
		handle = _handle;
	}

	public int getHandle() {
		return handle;
	}

	public int read(byte[] buf) {
		return read(buf, 0, buf.length);
	}

	public int read(byte[] buf, int offset, int count) {
		return PIGPIO.spiRead(handle, buf, offset, count);
	}

	public int write(byte[] buf) {
		return write(buf, 0, buf.length);
	}

	public int write(byte[] buf, int offset, int count) {
		return PIGPIO.spiWrite(handle, buf, offset, count);
	}

	public int transfer(byte[] txBuf, byte[] rxBuf) {
		return transfer(txBuf, 0, rxBuf, 0, rxBuf.length);
	}

	public int transfer(byte[] txBuf, int txOffset, byte[] rxBuf, int rxOffset, int count) {
		return PIGPIO.spiXfer(handle, txBuf, txOffset, rxBuf, rxOffset, count);
	}
}
