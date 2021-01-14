package com.lanternsoftware.zwave.servlet;

import com.lanternsoftware.datamodel.currentmonitor.AuthCode;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.zwave.context.Globals;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/temperature/*")
public class TemperatureServlet extends SecureServlet {
	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		String[] path = path(_req);
		DaoEntity json = new DaoEntity("temp", Globals.app.getTemperatureCelsius(NullUtils.toInteger(CollectionUtils.get(path, 0))));
		jsonResponse(_rep, DaoSerializer.toJson(json));
	}
}
