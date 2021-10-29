package com.lanternsoftware.zwave.servlet;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.zwave.context.Globals;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/removeNode/*")
public class RemoveNodeServlet extends SecureServlet {
	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		Globals.app.removeNodeFromNetwork(NullUtils.isEqual(CollectionUtils.get(path(_req), 0), "1"), DaoSerializer.toInteger(CollectionUtils.get(path(_req), 1)));
	}
}
