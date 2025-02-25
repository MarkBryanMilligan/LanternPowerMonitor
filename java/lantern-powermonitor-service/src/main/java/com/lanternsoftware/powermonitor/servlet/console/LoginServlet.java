package com.lanternsoftware.powermonitor.servlet.console;

import com.lanternsoftware.powermonitor.context.Globals;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends SecureConsoleServlet {
	@Override
	protected void get(HttpServletRequest _req, HttpServletResponse _rep) {
		render(_rep, "login.ftl", model(_req));
	}

	@Override
	protected void post(HttpServletRequest _req, HttpServletResponse _rep) {
		String username = _req.getParameter("username");
		String password = _req.getParameter("password");
		String authCode = Globals.dao.authenticateAccount(username, password);
		if (NullUtils.isNotEmpty(authCode)) {
			Cookie authCookie = new Cookie("auth_code", authCode);
			authCookie.setMaxAge(157680000);
			authCookie.setSecure(true);
			_rep.addCookie(authCookie);
			_req.getSession().setAttribute("auth_code", authCode);
			Cookie destination = CollectionUtils.filterOne(CollectionUtils.asArrayList(_req.getCookies()), _c-> NullUtils.isEqual(_c.getName(), "destination"));
			redirect(_rep, destination != null ? _req.getContextPath()+destination.getValue() : _req.getContextPath());
		}
		render(_rep, "login.ftl", model(_req, "error", "Invalid Credentials"));
	}
}
