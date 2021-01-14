package com.lanternsoftware.zwave.context;

import com.lanternsoftware.dataaccess.currentmonitor.MongoCurrentMonitorDao;
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.dao.mongo.MongoConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Globals implements ServletContextListener {
	public static ZWaveApp app;
	public static MongoCurrentMonitorDao cmDao;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		cmDao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.OPS_PATH + "mongo.cfg"));
		app = new ZWaveApp();
		app.start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (app != null) {
			app.stop();
			app = null;
		}
		if (cmDao != null)
			cmDao.shutdown();
	}
}
