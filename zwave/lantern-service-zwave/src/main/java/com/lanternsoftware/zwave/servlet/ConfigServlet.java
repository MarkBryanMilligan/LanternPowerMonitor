package com.lanternsoftware.zwave.servlet;

import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.zwave.context.Globals;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/config")
public class ConfigServlet extends SecureServlet {
	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		jsonResponse(_rep, Globals.app.getConfig());
	}
}
