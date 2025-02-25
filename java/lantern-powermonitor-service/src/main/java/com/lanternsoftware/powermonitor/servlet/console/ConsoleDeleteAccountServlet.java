package com.lanternsoftware.powermonitor.servlet.console;

import com.lanternsoftware.powermonitor.context.Globals;
import com.lanternsoftware.powermonitor.datamodel.Account;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/delete")
public class ConsoleDeleteAccountServlet extends AuthenticatedConsoleServlet {
	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		renderBody(_rep, "deleteAccount.ftl", model(_req, "inprogress", false));
	}

	@Override
	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		int accountId = DaoSerializer.toInteger(CollectionUtils.getFirst(_authCode.getAllAccountIds()));
		if (accountId == 0) {
			redirect(_rep, _req.getContextPath());
			return;
		}
		Account acct = Globals.dao.getAccount(accountId);
		if (acct == null) {
			redirect(_rep, _req.getContextPath());
			return;
		}
		Globals.dao.deleteAccount(accountId);
		_req.getSession().removeAttribute("auth_code");
		Cookie authCookie = new Cookie("auth_code", "");
		authCookie.setMaxAge(0);
		authCookie.setSecure(true);
		_rep.addCookie(authCookie);
		redirect(_rep, _req.getContextPath());
	}
}
