package com.lanternsoftware.thermometer;

import com.lanternsoftware.thermometer.context.ThermometerApp;

public class TestThermo {
	public static void main(String[] args) {
		ThermometerApp app = new ThermometerApp();
		app.start();
		try {
			Thread.sleep(20000);
		} catch (InterruptedException _e) {
			_e.printStackTrace();
		}
		app.stop();
	}
}
