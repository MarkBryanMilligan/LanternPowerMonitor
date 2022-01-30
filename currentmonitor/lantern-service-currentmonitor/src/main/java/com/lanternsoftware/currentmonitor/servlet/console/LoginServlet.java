package com.lanternsoftware.currentmonitor.servlet.console;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.currentmonitor.servlet.FreemarkerCMServlet;
import com.lanternsoftware.currentmonitor.util.GoogleAuthHelper;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.util.servlet.LanternServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends FreemarkerCMServlet {
	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		render(_rep, "login.ftl", model(_req));
	}

	@Override
	protected void doPost(HttpServletRequest _req, HttpServletResponse _rep) {
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
