package com.lanternsoftware.thermometer.context;

import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.concurrency.ConcurrencyUtils;
import org.hid4java.HidDevice;
import org.hid4java.HidManager;
import org.hid4java.HidServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

public class ThermometerApp {
	private static final Logger LOG = LoggerFactory.getLogger(ThermometerApp.class);

	private HidDevice device;
	private final Timer timer = new Timer();
	private double lastTemp;

	public void start() {
		HidServices hs = HidManager.getHidServices();
		for (HidDevice d : hs.getAttachedHidDevices()) {
			if (NullUtils.isEqual(d.getVendorId(), (short) 0x413d) && NullUtils.isEqual(d.getProductId(), (short) 0x2107)) {
				if (d.getInterfaceNumber() == 1)
					device = d;
			}
		}
		if ((device != null) && device.open()) {
			synchronized (device) {
				read(hexToByte("0182770100000000"));
				read(hexToByte("0186ff0100000000"));
				read(hexToByte("0182770100000000"));
				read(hexToByte("0182770100000000"));
			}
		} else {
			LOG.error("Failed to open HID Device");
			return;
		}
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				lastTemp = readTemperature();
			}
		}, 0L, 10000L);
	}

	public void stop() {
		timer.cancel();
		ConcurrencyUtils.sleep(10000);
		if (device != null) {
			device.close();
			device = null;
		}
		HidServices hs = HidManager.getHidServices();
		hs.stop();
		hs.shutdown();
	}

	private byte[] read(byte[] _request) {
		int RETRIES = 8;
		int stat = -1;
		int attempts = 0;
		while ((stat <= 0) && (attempts < RETRIES)) {
			attempts++;
			try {
				stat = device.write(_request, _request.length*8, (byte) 0);
			}
			catch (Exception _e) {
				LOG.error("Exception while writing", _e);
			}
			if (stat <= 0) {
				if (attempts == RETRIES) {
					LOG.error("Failed max number of retires, returning null");
					return null;
				}
				LOG.error("Write attempt " + attempts + " failed, waiting 250ms to retry");
				ConcurrencyUtils.sleep(250);
			}
		}
		byte[] response = new byte[32];
		stat = -1;
		attempts = 0;
		while ((stat <= 0) && (attempts < RETRIES)) {
			attempts++;
			try {
				stat = device.read(response, 500);
			}
			catch (Exception _e) {
				LOG.error("Exception while reading", _e);
			}
			if (stat <= 0) {
				if (attempts == RETRIES) {
					LOG.error("Failed max number of retires, returning null");
					return null;
				}
				LOG.error("Read attempt " + attempts + " failed, waiting 250ms to retry");
				ConcurrencyUtils.sleep(250);
			}
		}
		return response;
	}
	public double getTemperature() {
		return lastTemp;
	}

	public double readTemperature() {
		if (device != null) {
			synchronized (device) {
				byte[] response = read(hexToByte("0180330100000000"));
				if (response == null)
					return 5.0;
				int rawReading = ((response[3] & 0xFF) + (response[2] << 8));
				if (rawReading == 0)
					return 5.0;
				return rawReading / 100.0;
			}

		}
		return 5.0;
	}

	private static byte[] hexToByte(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
}
