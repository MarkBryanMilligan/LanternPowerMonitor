package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.datamodel.currentmonitor.AuthCode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class SecureServlet extends CMServlet {
	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		AuthCode authCode = Globals.dao.decryptAuthCode(_req.getHeader("auth_code"));
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
		AuthCode authCode = Globals.dao.decryptAuthCode(_req.getHeader("auth_code"));
		if (authCode == null) {
			_rep.setStatus(401);
			return;
		}
		post(authCode, _req, _rep);
	}

	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
	}
}
