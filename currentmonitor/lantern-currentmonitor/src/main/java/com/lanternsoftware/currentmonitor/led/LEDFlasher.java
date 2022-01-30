package com.lanternsoftware.currentmonitor.led;

import com.lanternsoftware.util.concurrency.ConcurrencyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class LEDFlasher implements Runnable {
	private static final Logger LOG = LoggerFactory.getLogger(LEDFlasher.class);

	private final AtomicBoolean running = new AtomicBoolean(true);
	private boolean on = false;

	@Override
	public void run() {
		while (running.get()) {
			on = !on;
			setLEDOn(on);
			ConcurrencyUtils.sleep(250);
		}
		setLEDOn(false);
	}

	public void stop() {
		running.set(false);
	}

	public static void setLEDOn(boolean _on) {
		try {
			if (_on) {
				Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo default-on > /sys/class/leds/led0/trigger"});
				Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo default-on > /sys/class/leds/led1/trigger"});
			}
			else {
				Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo none > /sys/class/leds/led0/trigger"});
				Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo none > /sys/class/leds/led1/trigger"});
			}
		}
		catch (Exception _e) {
			LOG.error("Failed to change LED state", _e);
		}
	}
}
