package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.datamodel.currentmonitor.hub.HubSample;
import com.lanternsoftware.util.dao.auth.AuthCode;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/sample")
public class SampleServlet extends SecureServiceServlet {
	@Override
	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		HubSample sample = getRequestPayload(_req, HubSample.class);
		if (sample == null)
			return;
		sample.setAccountId(_authCode.getAccountId());
		Globals.dao.putHubSample(sample);
	}
}
