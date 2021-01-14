package com.lanternsoftware.thermometer.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Globals implements ServletContextListener {
	public static ThermometerApp app;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		app = new ThermometerApp();
		app.start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (app != null) {
			app.stop();
			app = null;
		}
	}
}
