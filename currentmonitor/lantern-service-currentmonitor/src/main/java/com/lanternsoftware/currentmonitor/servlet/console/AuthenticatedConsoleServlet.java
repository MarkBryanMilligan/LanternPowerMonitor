package com.lanternsoftware.currentmonitor.servlet.console;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AuthenticatedConsoleServlet extends SecureConsoleServlet {
	@Override
	protected void get(HttpServletRequest _req, HttpServletResponse _rep) {
		AuthCode code = getAuthCode(_req, _rep);
		if (code != null)
			get(code, _req, _rep);
	}

	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
	}

	@Override
	protected void post(HttpServletRequest _req, HttpServletResponse _rep) {
		AuthCode code = getAuthCode(_req, _rep);
		if (code != null)
			post(code, _req, _rep);
	}

	private AuthCode getAuthCode(HttpServletRequest _req, HttpServletResponse _rep) {
		String sRequestURL = _req.getRequestURL().toString();
		String sURL = sRequestURL.replaceFirst("http://", "https://");
		if (!sURL.equals(sRequestURL)) {
			String sQuery = _req.getQueryString();
			if (NullUtils.isNotEmpty(sQuery))
				sURL += "?" + sQuery;
			redirect(_rep, sURL);
			return null;
		}
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
