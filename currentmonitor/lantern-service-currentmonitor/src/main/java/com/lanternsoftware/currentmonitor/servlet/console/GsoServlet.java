package com.lanternsoftware.currentmonitor.servlet.console;

import com.lanternsoftware.currentmonitor.servlet.FreemarkerCMServlet;
import com.lanternsoftware.currentmonitor.util.GoogleAuthHelper;
import com.lanternsoftware.util.NullUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/gso")
public class GsoServlet extends FreemarkerCMServlet {
	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		render(_rep, "login.ftl", model(_req));
	}

	@Override
	protected void doPost(HttpServletRequest _req, HttpServletResponse _rep) {
		String code = getRequestPayloadAsString(_req);
		if (NullUtils.isNotEmpty(code)) {
			String authCode = GoogleAuthHelper.signin(code, null);
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
