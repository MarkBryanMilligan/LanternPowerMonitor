package com.lanternsoftware.zwave.security;

import com.lanternsoftware.datamodel.zwave.Switch;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SecurityController {
	protected static final Logger LOG = LoggerFactory.getLogger(SecurityController.class);

	private final Map<Integer, GpioPinDigitalInput> pins = new HashMap<>();

	public boolean isOpen(int _pin) {
		GpioPinDigitalInput pin = getPin(_pin);
		return (pin == null) || pin.getState().isHigh();
	}

	public void listen(Switch _sw, SecurityListener _listener) {
		GpioPinDigitalInput pin = getPin(_sw.getGpioPin());
		if (pin != null)
			pin.addListener((GpioPinListenerDigital) _event -> _listener.onStateChanged(_sw.getNodeId(), _event.getState().isHigh()));
	}

	private GpioPinDigitalInput getPin(int _pin) {
		GpioPinDigitalInput pin = pins.get(_pin);
		if (pin == null) {
			pin = GpioFactory.getInstance().provisionDigitalInputPin(RaspiPin.getPinByAddress(_pin), "SecuritySensor", PinPullResistance.PULL_UP);
			if (pin != null)
				pins.put(_pin, pin);
			else {
				LOG.error("Failed to get pin {}", _pin);
				return null;
			}
		}
		return pin;
	}

	public void shutdown() {
		for (GpioPinDigitalInput pin : pins.values()) {
			pin.removeAllListeners();
		}
		pins.clear();
		GpioFactory.getInstance().shutdown();
	}
}
