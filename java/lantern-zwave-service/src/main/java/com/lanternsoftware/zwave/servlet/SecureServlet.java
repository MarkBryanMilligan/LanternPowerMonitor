package com.lanternsoftware.zwave.servlet;

import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.zwave.context.Globals;
import com.lanternsoftware.zwave.context.ZWaveApp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class SecureServlet extends ZWaveServlet {

	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		AuthCode authCode = DaoSerializer.fromZipBson(ZWaveApp.aes.decryptFromBase64(_req.getHeader("auth_code")), AuthCode.class);
		if ((authCode == null) || (authCode.getAccountId() != Globals.app.getAccountId())) {
			_rep.setStatus(401);
			return;
		}
		get(authCode, _req, _rep);
	}

	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
	}

	@Override
	protected void doPost(HttpServletRequest _req, HttpServletResponse _rep) {
		AuthCode authCode = DaoSerializer.fromZipBson(ZWaveApp.aes.decryptFromBase64(_req.getHeader("auth_code")), AuthCode.class);
		if ((authCode == null) || (authCode.getAccountId() != Globals.app.getAccountId())) {
			_rep.setStatus(401);
			return;
		}
		post(authCode, _req, _rep);
	}

	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
	}
}
