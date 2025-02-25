package com.lanternsoftware.powermonitor.servlet;

import com.lanternsoftware.powermonitor.context.Globals;
import com.lanternsoftware.powermonitor.datamodel.Account;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/deleteAccount")
public class DeleteAccountServlet extends SecureServiceServlet {
	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		int accountId = DaoSerializer.toInteger(CollectionUtils.getFirst(_authCode.getAllAccountIds()));
		if (accountId == 0) {
			_rep.setStatus(404);
			return;
		}
		Account acct = Globals.dao.getAccount(accountId);
		if (acct == null) {
			_rep.setStatus(404);
			return;
		}
		Globals.dao.deleteAccount(accountId);
		zipBsonResponse(_rep, new DaoEntity("success", true));
	}
}
