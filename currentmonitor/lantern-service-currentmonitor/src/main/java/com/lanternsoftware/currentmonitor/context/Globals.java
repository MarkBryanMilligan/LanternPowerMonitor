package com.lanternsoftware.currentmonitor.context;

import com.lanternsoftware.dataaccess.currentmonitor.CurrentMonitorDao;
import com.lanternsoftware.dataaccess.currentmonitor.MongoCurrentMonitorDao;
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.dao.mongo.MongoConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Globals implements ServletContextListener {
	public static CurrentMonitorDao dao;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		dao = new MongoCurrentMonitorDao(MongoConfig.fromDisk(LanternFiles.OPS_PATH + "mongo.cfg"));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		dao.shutdown();
	}
}
