package com.lanternsoftware.thermometer;

public interface IThermometer {
	double getTemperatureCelsius();
	boolean isConnected();
	void shutdown();
}
