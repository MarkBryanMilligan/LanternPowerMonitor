package com.lanternsoftware.currentmonitor.servlet.console;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/logout")
public class LogoutServlet extends AuthenticatedConsoleServlet {
	@Override
	protected void get(HttpServletRequest _req, HttpServletResponse _rep) {
		_req.getSession().removeAttribute("auth_code");
		Cookie authCookie = new Cookie("auth_code", "");
		authCookie.setMaxAge(0);
		authCookie.setSecure(true);
		_rep.addCookie(authCookie);
		redirect(_rep, _req.getContextPath());
	}

	@Override
	protected void post(HttpServletRequest _req, HttpServletResponse _rep) {
	}
}
