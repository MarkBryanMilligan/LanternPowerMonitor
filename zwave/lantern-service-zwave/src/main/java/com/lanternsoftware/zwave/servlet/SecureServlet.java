package com.lanternsoftware.zwave.servlet;

import com.lanternsoftware.datamodel.currentmonitor.AuthCode;
import com.lanternsoftware.zwave.context.Globals;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class SecureServlet extends ZWaveServlet {
	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		AuthCode authCode = Globals.cmDao.decryptAuthCode(_req.getHeader("auth_code"));
		if ((authCode == null) || (authCode.getAccountId() != 1)) {
			_rep.setStatus(401);
			return;
		}
		get(authCode, _req, _rep);
	}

	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
	}

	@Override
	protected void doPost(HttpServletRequest _req, HttpServletResponse _rep) {
		AuthCode authCode = Globals.cmDao.decryptAuthCode(_req.getHeader("auth_code"));
		if ((authCode == null) || (authCode.getAccountId() != 1)) {
			_rep.setStatus(401);
			return;
		}
		post(authCode, _req, _rep);
	}

	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
	}
}
