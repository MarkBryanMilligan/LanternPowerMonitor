package com.lanternsoftware.zwave.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Globals implements ServletContextListener {
	public static ZWaveApp app;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		app = new ZWaveApp();
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
