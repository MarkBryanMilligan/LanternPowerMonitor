package com.lanternsoftware.powermonitor.servlet;

import com.lanternsoftware.powermonitor.context.Globals;
import com.lanternsoftware.powermonitor.datamodel.BreakerConfig;
import com.lanternsoftware.powermonitor.datamodel.HubCommand;
import com.lanternsoftware.powermonitor.datamodel.HubConfigCharacteristic;
import com.lanternsoftware.rules.RulesEngine;
import com.lanternsoftware.util.dao.auth.AuthCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/config/*")
public class ConfigServlet extends SecureServiceServlet {
	private static final Logger logger = LoggerFactory.getLogger(ConfigServlet.class);

	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		if (isPath(_req, 0, "bin"))
			zipBsonResponse(_rep, Globals.dao.getMergedConfig(_authCode));
		else
			jsonResponse(_rep, Globals.dao.getMergedConfig(_authCode));
	}

	@Override
	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		BreakerConfig config = getRequestPayload(_req, BreakerConfig.class);
		if (config == null) {
			_rep.setStatus(400);
			return;
		}
		if (config.getAccountId() != _authCode.getAccountId()) {
			_rep.setStatus(401);
			return;
		}
		logger.info("Received config for account {}", config.getAccountId());
		BreakerConfig oldConfig = Globals.dao.getConfig(config.getAccountId());
		if ((oldConfig == null) || !oldConfig.isIdentical(config))
			Globals.dao.putHubCommand(new HubCommand(config.getAccountId(), HubConfigCharacteristic.ReloadConfig, null));
		Globals.dao.putConfig(config);
		config = Globals.dao.getMergedConfig(_authCode);
		RulesEngine.instance().sendFcmMessage(config.getAccountId(), config);
		zipBsonResponse(_rep, config);
	}
}
