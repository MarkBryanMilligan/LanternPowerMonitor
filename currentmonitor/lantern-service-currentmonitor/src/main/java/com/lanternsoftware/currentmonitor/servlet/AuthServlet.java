package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.currentmonitor.util.GoogleAuthHelper;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.servlet.BasicAuth;
import com.lanternsoftware.util.servlet.LanternServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/auth/*")
public class AuthServlet extends LanternServlet {
	private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);

	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		String authCode = _req.getHeader("auth_code");
		if (NullUtils.isEmpty(authCode)) {
			BasicAuth auth = new BasicAuth(_req);
			if (NullUtils.isEqual(auth.getUsername(), "googlesso")) {
				logger.info("Attempting google SSO");
				authCode = GoogleAuthHelper.signin(auth.getPassword(), DateUtils.fromTimeZoneId(_req.getHeader("timezone")));
			} else
				authCode = Globals.dao.authenticateAccount(auth.getUsername(), auth.getPassword());
		}
		DaoEntity rep = new DaoEntity("auth_code", authCode).and("timezone", Globals.dao.getTimeZoneForAccount(authCode));
		if (isPath(_req, 0, "bin"))
			zipBsonResponse(_rep, rep);
		else
			jsonResponse(_rep, rep);
	}
}
