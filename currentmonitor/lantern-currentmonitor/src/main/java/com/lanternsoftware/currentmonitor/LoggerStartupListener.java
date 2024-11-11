package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoSerializer;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.LifeCycle;

public class LoggerStartupListener extends ContextAwareBase implements LoggerContextListener, LifeCycle {
    
	private static final String WORKING_DIR = "/opt/currentmonitor/";

    private boolean started = false;

    @Override
    public void start() {
        if (started) return;

		MonitorConfig config = DaoSerializer.parse(ResourceLoader.loadFileAsString(WORKING_DIR + "config.json"), MonitorConfig.class);
		if (config == null) {
			config = new MonitorConfig();
			ResourceLoader.writeFile(WORKING_DIR + "config.json", DaoSerializer.toJson(config));
		}

        Context context = getContext();

        context.putProperty("LOKI_URL", config.getLokiUrl());

        started = true;
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isResetResistant() {
        return true;
    }

    @Override
    public void onStart(LoggerContext context) {
    }

    @Override
    public void onReset(LoggerContext context) {
    }

    @Override
    public void onStop(LoggerContext context) {
    }

    @Override
    public void onLevelChange(Logger arg0, Level arg1) {
    }
}
