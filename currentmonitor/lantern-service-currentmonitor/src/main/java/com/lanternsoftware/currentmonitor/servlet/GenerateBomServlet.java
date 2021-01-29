package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.datamodel.currentmonitor.AuthCode;
import com.lanternsoftware.util.dao.DaoEntity;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/generateBom")
public class GenerateBomServlet extends SecureServlet {
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
