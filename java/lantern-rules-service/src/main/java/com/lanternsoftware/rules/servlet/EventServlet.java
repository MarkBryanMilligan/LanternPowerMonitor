package com.lanternsoftware.rules.servlet;


import com.lanternsoftware.datamodel.rules.Event;
import com.lanternsoftware.rules.RulesEngine;
import com.lanternsoftware.util.dao.auth.AuthCode;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/event")
public class EventServlet extends SecureServlet {
	@Override
	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		Event event = getRequestPayload(_req, Event.class);
		if (event == null) {
			_rep.setStatus(400);
			return;
		}
		event.setAccountId(_authCode.getAccountId());
		RulesEngine.instance().fireEvent(event);
	}
}
