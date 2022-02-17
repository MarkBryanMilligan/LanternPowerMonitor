package com.lanternsoftware.currentmonitor.servlet.console;

import com.lanternsoftware.util.dao.auth.AuthCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("")
public class ConsoleServlet extends AuthenticatedConsoleServlet {
	private static final Logger logger = LoggerFactory.getLogger(ConsoleServlet.class);

	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		redirect(_rep, "export");
	}
}
