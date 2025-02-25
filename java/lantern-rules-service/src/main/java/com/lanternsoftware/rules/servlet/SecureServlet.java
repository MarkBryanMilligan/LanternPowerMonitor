package com.lanternsoftware.rules.servlet;

import com.lanternsoftware.util.cryptography.AESTool;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.util.servlet.LanternServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class SecureServlet extends LanternServlet {
	private static final AESTool aes = AESTool.authTool();

	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		AuthCode authCode = DaoSerializer.fromZipBson(aes.decryptFromBase64(_req.getHeader("auth_code")), AuthCode.class);
		if (authCode == null) {
			_rep.setStatus(401);
			return;
		}
		get(authCode, _req, _rep);
	}

	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
	}

	@Override
	protected void doPost(HttpServletRequest _req, HttpServletResponse _rep) {
		AuthCode authCode = DaoSerializer.fromZipBson(aes.decryptFromBase64(_req.getHeader("auth_code")), AuthCode.class);
		if (authCode == null) {
			_rep.setStatus(401);
			return;
		}
		post(authCode, _req, _rep);
	}

	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
	}
}
