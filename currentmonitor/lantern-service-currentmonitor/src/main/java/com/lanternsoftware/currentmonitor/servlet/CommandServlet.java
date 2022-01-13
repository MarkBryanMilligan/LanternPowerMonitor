package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.util.dao.auth.AuthCode;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/command")
public class CommandServlet extends SecureServlet {

	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
	}

	@Override
	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
	}
}
