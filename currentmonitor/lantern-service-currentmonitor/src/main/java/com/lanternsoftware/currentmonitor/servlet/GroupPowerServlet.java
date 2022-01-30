package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/power/group/*")
public class GroupPowerServlet extends SecureServiceServlet {
	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		String[] path = path(_req);
		if (path.length < 1)
			zipBsonResponse(_rep, new DaoEntity("breakers", DaoSerializer.toDaoEntities(Globals.dao.getBreakerPowerForAccount(_authCode.getAccountId()))));
	}
}
