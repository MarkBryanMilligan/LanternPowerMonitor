package com.lanternsoftware.pigpio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PiGpioFactory {
	private static final Logger LOG = LoggerFactory.getLogger(PIGPIO.class);
	private static final Map<Integer, Spi> spiHandles = new HashMap<>();
	private static boolean initialized = false;

	public static Spi getSpiChannel(int _chipSelect, int _baud, boolean _auxiliaryChannel) {
		if (!ensureInitialized())
			return null;
		int channelId = (0xff & _chipSelect);
		if (_auxiliaryChannel)
			channelId |= 0x100;
		Spi handle = spiHandles.get(channelId);
		if (handle != null)
			return handle;
		int h = PIGPIO.spiOpen(_chipSelect, _baud, _auxiliaryChannel ? 0x100 : 0);
		if (h >= 0) {
			handle = new Spi(h);
			spiHandles.put(channelId, handle);
			return handle;
		}
		LOG.error("Failed to get SPI handle");
		return null;
	}

	public static boolean ensureInitialized() {
		if (initialized)
			return true;
		int init = PIGPIO.gpioInitialise();
		LOG.info("GPIO init: {}", init);
		if (init < 0) {
			LOG.error("Failed to initialize PiGpio");
			return false;
		}
		initialized = true;
		return true;
	}

	public static void shutdown() {
		for (Spi handle : spiHandles.values()) {
			PIGPIO.spiClose(handle.getHandle());
		}
		spiHandles.clear();
		PIGPIO.gpioTerminate();
	}
}
