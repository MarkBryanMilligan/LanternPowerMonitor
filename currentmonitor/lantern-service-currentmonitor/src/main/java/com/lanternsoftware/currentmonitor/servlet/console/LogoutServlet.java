package com.lanternsoftware.currentmonitor.servlet.console;

import com.lanternsoftware.currentmonitor.servlet.FreemarkerCMServlet;
import com.lanternsoftware.currentmonitor.util.GoogleAuthHelper;
import com.lanternsoftware.util.NullUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/logout")
public class LogoutServlet extends FreemarkerCMServlet {
	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		_req.getSession().removeAttribute("auth_code");
		Cookie authCookie = new Cookie("auth_code", "");
		authCookie.setMaxAge(0);
		authCookie.setSecure(true);
		_rep.addCookie(authCookie);
		redirect(_rep, _req.getContextPath());
	}

	@Override
	protected void doPost(HttpServletRequest _req, HttpServletResponse _rep) {
	}
}
