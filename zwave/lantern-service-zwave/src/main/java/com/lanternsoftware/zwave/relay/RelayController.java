package com.lanternsoftware.zwave.relay;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RelayController {
	protected static final Logger LOG = LoggerFactory.getLogger(RelayController.class);

	private final Map<Integer, GpioPinDigitalOutput> pins = new HashMap<>();

	public void setRelay(int _pin, boolean _on) {
		GpioPinDigitalOutput pin = pins.get(_pin);
		if (pin == null) {
			pin = GpioFactory.getInstance().provisionDigitalOutputPin(RaspiPin.getPinByAddress(_pin), "Relay", PinState.LOW);
			if (pin != null)
				pins.put(_pin, pin);
			else {
				LOG.error("Failed to get pin {}", _pin);
				return;
			}
		}
		LOG.info("Setting pin {} to {}", _pin, _on);
		if (_on)
			pin.high();
		else
			pin.low();
	}

	public void shutdown() {
		GpioFactory.getInstance().shutdown();
	}
}
