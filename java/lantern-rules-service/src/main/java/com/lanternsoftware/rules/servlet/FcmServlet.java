package com.lanternsoftware.rules.servlet;


import com.lanternsoftware.datamodel.rules.FcmDevice;
import com.lanternsoftware.rules.RulesEngine;
import com.lanternsoftware.util.dao.auth.AuthCode;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/fcm")
public class FcmServlet extends SecureServlet {
	@Override
	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		FcmDevice device = getRequestPayload(_req, FcmDevice.class);
		if (device == null) {
			_rep.setStatus(400);
			return;
		}
		device.setAccountId(_authCode.getAccountId());
		RulesEngine.instance().dao().putFcmDevice(device);
	}
}
