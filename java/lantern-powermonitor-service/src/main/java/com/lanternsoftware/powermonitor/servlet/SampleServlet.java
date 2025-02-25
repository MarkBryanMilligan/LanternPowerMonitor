package com.lanternsoftware.powermonitor.servlet;

import com.lanternsoftware.powermonitor.context.Globals;
import com.lanternsoftware.powermonitor.datamodel.hub.HubSample;
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
