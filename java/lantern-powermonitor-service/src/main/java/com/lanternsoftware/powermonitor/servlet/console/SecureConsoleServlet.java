package com.lanternsoftware.powermonitor.servlet.console;

import com.lanternsoftware.powermonitor.servlet.FreemarkerCMServlet;
import com.lanternsoftware.util.NullUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class SecureConsoleServlet extends FreemarkerCMServlet {
	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		if (isSecure(_req, _rep))
			get(_req, _rep);
	}

	protected void get(HttpServletRequest _req, HttpServletResponse _rep) {
	}

	@Override
	protected void doPost(HttpServletRequest _req, HttpServletResponse _rep) {
		if (isSecure(_req, _rep))
			post(_req, _rep);
	}

	protected void post(HttpServletRequest _req, HttpServletResponse _rep) {
	}

	private boolean isSecure(HttpServletRequest _req, HttpServletResponse _rep) {
		String sRequestURL = _req.getRequestURL().toString();
		String sURL = sRequestURL.replaceFirst("http://", "https://");
		if (!sURL.equals(sRequestURL)) {
			String sQuery = _req.getQueryString();
			if (NullUtils.isNotEmpty(sQuery))
				sURL += "?" + sQuery;
			redirect(_rep, sURL);
			return false;
		}
		return true;
	}
}
