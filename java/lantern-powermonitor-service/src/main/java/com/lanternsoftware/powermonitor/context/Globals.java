package com.lanternsoftware.powermonitor.context;

import com.lanternsoftware.powermonitor.dataaccess.PowerMonitorDao;
import com.lanternsoftware.powermonitor.dataaccess.MongoPowerMonitorDao;
import com.lanternsoftware.powermonitor.datamodel.HubCommand;
import com.lanternsoftware.powermonitor.datamodel.HubCommands;
import com.lanternsoftware.rules.RulesEngine;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.dao.mongo.MongoConfig;
import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.http.HttpFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Globals implements ServletContextListener {
	public static PowerMonitorDao dao;
	public static ExecutorService opsExecutor;
	private static final Map<Integer, Map<Integer, List<HubCommand>>> commands = new HashMap<>();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		dao = new MongoPowerMonitorDao(MongoConfig.fromDisk(LanternFiles.CONFIG_PATH + "mongo.cfg"));
		RulesEngine.instance().start();
		RulesEngine.instance().schedule(new CommandTask(), 0);
		opsExecutor = Executors.newFixedThreadPool(7);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		opsExecutor.shutdown();
		dao.shutdown();
		HttpFactory.shutdown();
		RulesEngine.shutdown();
	}

	public static HubCommands getCommandsForHub(int _accountId, int _hub) {
		List<HubCommand> c = null;
		synchronized (commands) {
			Map<Integer, List<HubCommand>> hubCommands = commands.get(_accountId);
			if (hubCommands != null)
				c = hubCommands.remove(_hub);
		}
		if (c != null) {
			for (HubCommand command : c) {
				dao.deleteHubCommand(command.getId());
			}
			return new HubCommands(c);
		}
		return null;
	}

	private static final class CommandTask extends TimerTask {
		@Override
		public void run() {
			List<HubCommand> c = Globals.dao.getAllHubCommands();
			Date stale = DateUtils.addMinutes(new Date(), -5);
			synchronized (commands) {
				commands.clear();
				for (HubCommand command : c) {
					if (DateUtils.isBefore(command.getCreated(), stale))
						dao.deleteHubCommand(command.getId());
					else
						commands.computeIfAbsent(command.getAccountId(), _t -> new HashMap<>()).computeIfAbsent(command.getHub(), _h->new ArrayList<>()).add(command);
				}
			}
			RulesEngine.instance().schedule(new CommandTask(), 1000);
		}
	}
}
