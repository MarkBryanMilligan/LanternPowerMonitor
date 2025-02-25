package com.lanternsoftware.thermometer;

import com.lanternsoftware.util.CollectionUtils;
import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.component.temperature.impl.TmpDS18B20DeviceType;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;

import java.util.List;

public class DS18B20Thermometer implements IThermometer {
	W1Device device;

	public static List<DS18B20Thermometer> devices() {
		W1Master master = new W1Master();
		return CollectionUtils.transform(master.getDevices(TmpDS18B20DeviceType.FAMILY_CODE), DS18B20Thermometer::new);
	}

	public DS18B20Thermometer() {
		W1Master master = new W1Master();
		device = CollectionUtils.getFirst(master.getDevices(TmpDS18B20DeviceType.FAMILY_CODE));
	}

	public DS18B20Thermometer(W1Device _device) {
		device = _device;
	}

	@Override
	public double getTemperatureCelsius() {
		return device == null?-273:((TemperatureSensor) device).getTemperature();
	}

	@Override
	public boolean isConnected() {
		return device != null;
	}

	@Override
	public void shutdown() {
	}
}
