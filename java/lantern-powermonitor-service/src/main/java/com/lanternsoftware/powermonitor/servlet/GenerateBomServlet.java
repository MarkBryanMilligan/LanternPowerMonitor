package com.lanternsoftware.powermonitor.servlet;

import com.lanternsoftware.powermonitor.context.Globals;
import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.util.dao.DaoEntity;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/generateBom")
public class GenerateBomServlet extends SecureServiceServlet {
	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		AuthCode authCode = Globals.dao.decryptAuthCode(_req.getHeader("auth_code"));
		if (authCode == null) {
			_rep.setStatus(401);
			return;
		}
		String id = Globals.dao.getProxy().saveEntity("bom", new DaoEntity("acct_id", authCode.getAccountId()));
		jsonResponse(_rep, new DaoEntity("link", "bom/" + id));
	}
}
