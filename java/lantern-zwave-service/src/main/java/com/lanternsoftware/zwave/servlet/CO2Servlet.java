package com.lanternsoftware.zwave.servlet;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.zwave.context.Globals;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/co2/*")
public class CO2Servlet extends SecureServlet {
	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		String[] path = path(_req);
		jsonResponse(_rep, DaoSerializer.toJson(new DaoEntity("ppm", Globals.app.getCO2ppm(NullUtils.toInteger(CollectionUtils.get(path, 0))))));
	}
}
