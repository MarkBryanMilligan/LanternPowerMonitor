package com.lanternsoftware.thermometer.context;

import com.lanternsoftware.thermometer.DS18B20Thermometer;
import com.lanternsoftware.thermometer.HidThermometer;
import com.lanternsoftware.thermometer.ICO2Sensor;
import com.lanternsoftware.thermometer.IThermometer;
import com.lanternsoftware.thermometer.MHZ19BCO2Sensor;
import com.lanternsoftware.thermometer.config.EnvironmentConfig;
import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoSerializer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.List;

public class Globals implements ServletContextListener {
	private static EnvironmentConfig config;
	public static List<IThermometer> thermometers = new ArrayList<>();
	public static ICO2Sensor co2Sensor;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		config = DaoSerializer.parse(ResourceLoader.loadFile(LanternFiles.CONFIG_PATH + "environment.json"), EnvironmentConfig.class);
		IThermometer t = new HidThermometer();
		if (t.isConnected())
			thermometers.add(t);
		thermometers.addAll(DS18B20Thermometer.devices());
		if ((config != null) && NullUtils.isNotEmpty(config.getCo2serialPort()))
			co2Sensor = new MHZ19BCO2Sensor(config.getCo2serialPort());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		thermometers.forEach(IThermometer::shutdown);
		if (co2Sensor != null)
			co2Sensor.shutdown();
	}
}
