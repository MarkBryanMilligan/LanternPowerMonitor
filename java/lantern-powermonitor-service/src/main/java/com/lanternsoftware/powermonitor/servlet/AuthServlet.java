package com.lanternsoftware.powermonitor.servlet;

import com.lanternsoftware.powermonitor.context.Globals;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.cloudservices.apple.AppleSSO;
import com.lanternsoftware.util.cloudservices.google.GoogleSSO;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.external.LanternFiles;
import com.lanternsoftware.util.servlet.BasicAuth;
import com.lanternsoftware.util.servlet.LanternServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/auth/*")
public class AuthServlet extends LanternServlet {
	private static final GoogleSSO googleSSO = new GoogleSSO(LanternFiles.CONFIG_PATH + "google_sso.txt");
	private static final AppleSSO appleSSO = new AppleSSO(LanternFiles.CONFIG_PATH + "apple_sso.txt");

	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		String authCode = _req.getHeader("auth_code");
		String idToken = _req.getHeader("id_token");
		String email = null;
		if (NullUtils.isNotEmpty(idToken))
			email = appleSSO.getEmailFromIdToken(idToken);
		else if (NullUtils.isNotEmpty(authCode))
			authCode = Globals.dao.exchangeAuthCode(authCode, DaoSerializer.toInteger(_req.getHeader("override_account")));
		else {
			BasicAuth auth = new BasicAuth(_req);
			if (NullUtils.isEqual(auth.getUsername(), "googlesso"))
				email = googleSSO.signin(auth.getPassword());
			else
				authCode = Globals.dao.authenticateAccount(auth.getUsername(), auth.getPassword());
		}
		if (NullUtils.isNotEmpty(email))
			authCode = Globals.dao.getAuthCodeForEmail(email, DateUtils.fromTimeZoneId(_req.getHeader("timezone")));
		DaoEntity rep = new DaoEntity("auth_code", authCode).and("timezone", Globals.dao.getTimeZoneForAccount(authCode));
		if (isPath(_req, 0, "bin"))
			zipBsonResponse(_rep, rep);
		else
			jsonResponse(_rep, rep);
	}
}
