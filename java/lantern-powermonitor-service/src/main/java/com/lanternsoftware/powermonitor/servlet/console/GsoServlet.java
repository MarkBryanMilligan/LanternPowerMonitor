package com.lanternsoftware.powermonitor.servlet.console;

import com.lanternsoftware.powermonitor.context.Globals;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.cloudservices.google.GoogleSSO;
import com.lanternsoftware.util.external.LanternFiles;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/gso")
public class GsoServlet extends SecureConsoleServlet {
	private static final GoogleSSO googleSSO = new GoogleSSO(LanternFiles.CONFIG_PATH + "google_sso.txt");

	@Override
	protected void get(HttpServletRequest _req, HttpServletResponse _rep) {
		render(_rep, "login.ftl", model(_req));
	}

	@Override
	protected void post(HttpServletRequest _req, HttpServletResponse _rep) {
		String code = getRequestPayloadAsString(_req);
		if (NullUtils.isNotEmpty(code)) {
			String email = googleSSO.signin(code);
			if (NullUtils.isNotEmpty(email)) {
				String authCode = Globals.dao.getAuthCodeForEmail(email, DateUtils.fromTimeZoneId(_req.getHeader("timezone")));
				if (NullUtils.isNotEmpty(authCode)) {
					Cookie authCookie = new Cookie("auth_code", authCode);
					authCookie.setMaxAge(157680000);
					authCookie.setSecure(true);
					_rep.addCookie(authCookie);
					_req.getSession().setAttribute("auth_code", authCode);
				}
			}
		}
	}
}
