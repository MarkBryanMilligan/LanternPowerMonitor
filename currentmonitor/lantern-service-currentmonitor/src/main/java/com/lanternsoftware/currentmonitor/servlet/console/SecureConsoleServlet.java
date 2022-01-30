package com.lanternsoftware.currentmonitor.servlet.console;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.currentmonitor.servlet.FreemarkerCMServlet;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class SecureConsoleServlet extends FreemarkerCMServlet {
	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		AuthCode code = getAuthCode(_req, _rep);
		if (code != null)
			get(code, _req, _rep);
	}

	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
	}

	@Override
	protected void doPost(HttpServletRequest _req, HttpServletResponse _rep) {
		AuthCode code = getAuthCode(_req, _rep);
		if (code != null)
			post(code, _req, _rep);
	}

	private AuthCode getAuthCode(HttpServletRequest _req, HttpServletResponse _rep) {
		AuthCode authCode = Globals.dao.decryptAuthCode(DaoSerializer.toString(_req.getSession().getAttribute("auth_code")));
		if (authCode == null) {
			Cookie authCookie = CollectionUtils.filterOne(CollectionUtils.asArrayList(_req.getCookies()), _c-> NullUtils.isEqual(_c.getName(), "auth_code"));
			if (authCookie != null)
				authCode = Globals.dao.decryptAuthCode(authCookie.getValue());
		}
		if (authCode == null) {
			redirect(_rep, _req.getContextPath() + "/login");
			return null;
		}
		return authCode;
	}

	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
	}
}
