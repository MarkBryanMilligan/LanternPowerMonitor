package com.lanternsoftware.thermometer;

import com.lanternsoftware.util.concurrency.ConcurrencyUtils;

public class TestStartup {
	public static void main(String[] args) {
		IThermometer thermometer = new DS18B20Thermometer();
		for (int i=0; i<200; i++) {
			System.out.println(String.format("%.2f", thermometer.getTemperatureCelsius()));
			ConcurrencyUtils.sleep(1000);
		}
		thermometer.shutdown();
	}
}
