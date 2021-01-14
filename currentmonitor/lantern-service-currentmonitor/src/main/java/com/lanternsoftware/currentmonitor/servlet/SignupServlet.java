package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.datamodel.currentmonitor.Account;
import com.lanternsoftware.datamodel.currentmonitor.SignupResponse;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.email.EmailValidator;
import com.lanternsoftware.util.servlet.BasicAuth;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/signup")
public class SignupServlet extends CMServlet {
	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		BasicAuth auth = new BasicAuth(_req);
		Account acct = Globals.dao.getAccountByUsername(auth.getUsername());
		if (acct != null) {
			jsonResponse(_rep, SignupResponse.error("An account for " + auth.getUsername() + " already exists"));
			return;
		}
		if (!EmailValidator.getInstance().isValid(auth.getUsername())) {
			jsonResponse(_rep, SignupResponse.error(auth.getUsername() + " is not a valid email address"));
			return;
		}
		if (NullUtils.length(auth.getPassword()) < 8) {
			jsonResponse(_rep, SignupResponse.error("Your password must be at least 8 characters long"));
			return;
		}
		if (NullUtils.isEqual("password", auth.getPassword())) {
			jsonResponse(_rep, SignupResponse.error("Seriously?  \"password\"?  Come on."));
			return;
		}
		acct = new Account();
		acct.setUsername(auth.getUsername());
		acct.setPassword(auth.getPassword());
		Globals.dao.putAccount(acct);
		String authCode = Globals.dao.authenticateAccount(auth.getUsername(), auth.getPassword());
		jsonResponse(_rep, SignupResponse.success(authCode));
	}
}
