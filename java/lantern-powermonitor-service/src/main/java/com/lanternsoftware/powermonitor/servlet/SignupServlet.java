package com.lanternsoftware.powermonitor.servlet;

import com.lanternsoftware.powermonitor.context.Globals;
import com.lanternsoftware.powermonitor.datamodel.Account;
import com.lanternsoftware.powermonitor.datamodel.SignupResponse;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.email.EmailValidator;
import com.lanternsoftware.util.servlet.BasicAuth;
import com.lanternsoftware.util.servlet.LanternServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/signup/*")
public class SignupServlet extends LanternServlet {
	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		String tz = DateUtils.fromTimeZoneId(_req.getHeader("timezone")).getID();
		boolean binary = isPath(_req, 0, "bin");
		if (Globals.dao.isTimeZoneBlacklisted(tz)) {
			jsonResponse(_rep, SignupResponse.error("Unauthorized"), binary);
			return;
		}
		BasicAuth auth = new BasicAuth(_req);
		Account acct = Globals.dao.getAccountByUsername(auth.getUsername().toLowerCase().trim());
		if (acct != null) {
			jsonResponse(_rep, SignupResponse.error("An account for " + auth.getUsername() + " already exists"), binary);
			return;
		}
		if (!EmailValidator.getInstance().isValid(auth.getUsername())) {
			jsonResponse(_rep, SignupResponse.error(auth.getUsername() + " is not a valid email address"), binary);
			return;
		}
		if (NullUtils.length(auth.getPassword()) < 8) {
			jsonResponse(_rep, SignupResponse.error("Your password must be at least 8 characters long"), binary);
			return;
		}
		if (NullUtils.isEqual("password", auth.getPassword())) {
			jsonResponse(_rep, SignupResponse.error("Seriously?  \"password\"?  Come on."), binary);
			return;
		}
		acct = new Account();
		acct.setUsername(auth.getUsername());
		acct.setPassword(auth.getPassword());
		acct.setTimezone(tz);
		Globals.dao.putAccount(acct);
		String authCode = Globals.dao.authenticateAccount(auth.getUsername(), auth.getPassword());
		jsonResponse(_rep, SignupResponse.success(authCode, acct.getTimezone()), binary);
	}
}
