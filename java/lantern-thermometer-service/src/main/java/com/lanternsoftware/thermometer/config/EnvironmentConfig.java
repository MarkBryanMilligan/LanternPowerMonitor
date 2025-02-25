package com.lanternsoftware.thermometer.config;

import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class EnvironmentConfig {
	private String co2serialPort;

	public String getCo2serialPort() {
		return co2serialPort;
	}

	public void setCo2serialPort(String _co2serialPort) {
		co2serialPort = _co2serialPort;
	}
}
