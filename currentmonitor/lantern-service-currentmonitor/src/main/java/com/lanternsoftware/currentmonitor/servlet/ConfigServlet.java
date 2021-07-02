package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.dataaccess.currentmonitor.MongoCurrentMonitorDao;
import com.lanternsoftware.datamodel.currentmonitor.AuthCode;
import com.lanternsoftware.datamodel.currentmonitor.BreakerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/config/*")
public class ConfigServlet extends SecureServlet {
	private static final Logger logger = LoggerFactory.getLogger(ConfigServlet.class);

	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		if (_authCode.getAccountId() == 100) {
			logger.error("my ip: " + _req.getRemoteAddr());
		}
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
		Globals.dao.putConfig(config);
	}
}
