package com.lanternsoftware.powermonitor.servlet.console;

import com.lanternsoftware.powermonitor.context.Globals;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.cloudservices.google.GoogleSSO;
import com.lanternsoftware.util.cryptography.AESTool;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.external.LanternFiles;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/gso")
public class GsoServlet extends SecureConsoleServlet {
	private static final Logger logger = LoggerFactory.getLogger(GsoServlet.class);
	private static final GoogleSSO googleSSO = new GoogleSSO(LanternFiles.CONFIG_PATH + "google_sso.txt");

	@Override
	protected void get(HttpServletRequest _req, HttpServletResponse _rep) {
		String code = _req.getParameter("code");
		if (NullUtils.isNotEmpty(code)) {
			String email = googleSSO.signin(code);
			if (NullUtils.isNotEmpty(email)) {
				String authCode = Globals.dao.getAuthCodeForEmail(email, DateUtils.fromTimeZoneId(_req.getHeader("timezone")));
				if (NullUtils.isNotEmpty(authCode)) {
					logger.error("Successfully generated auth code for user {}", email);
					Cookie authCookie = new Cookie("auth_code", authCode);
					authCookie.setMaxAge(157680000);
					authCookie.setSecure(true);
					_rep.addCookie(authCookie);
					_req.getSession().setAttribute("auth_code", authCode);
					redirect(_rep, "export");
					return;
				}
				logger.error("Failed to generate authCode for user {}", email);
			}
			else
				logger.error("Failed to retrieve email from google for auth code {}", code);
		}
		else
			logger.error("Auth code from google is empty");
		DaoEntity model = model(_req);
		model.put("state", Hex.encodeHexString(AESTool.randomIV()));
		render(_rep, "login.ftl", model);
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
