package com.lanternsoftware.currentmonitor.servlet.console;

import com.lanternsoftware.currentmonitor.context.Globals;
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
			redirect(_rep, _req.getContextPath());
		}
		render(_rep, "login.ftl", model(_req, "error", "Invalid Credentials"));
	}
}
