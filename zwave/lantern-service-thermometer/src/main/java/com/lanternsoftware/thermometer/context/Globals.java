package com.lanternsoftware.thermometer.context;

import com.lanternsoftware.thermometer.DS18B20Thermometer;
import com.lanternsoftware.thermometer.HidThermometer;
import com.lanternsoftware.thermometer.IThermometer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.List;

public class Globals implements ServletContextListener {
	public static List<IThermometer> thermometers = new ArrayList<>();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		IThermometer t = new HidThermometer();
		if (t.isConnected())
			thermometers.add(t);
		thermometers.addAll(DS18B20Thermometer.devices());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		thermometers.forEach(IThermometer::shutdown);
	}
}
