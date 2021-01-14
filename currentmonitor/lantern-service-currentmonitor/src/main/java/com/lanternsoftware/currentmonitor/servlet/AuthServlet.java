package com.lanternsoftware.currentmonitor.servlet;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.servlet.BasicAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

@WebServlet("/auth/*")
public class AuthServlet extends CMServlet {
	private static final NetHttpTransport transport = new NetHttpTransport();
	private static final JacksonFactory jsonFactory = new JacksonFactory();
	private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);
	private static final String googleSsoKey = ResourceLoader.loadFileAsString(LanternFiles.OPS_PATH + "google_sso_key.txt");

	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		String authCode = _req.getHeader("auth_code");
		if (NullUtils.isEmpty(authCode)) {
			BasicAuth auth = new BasicAuth(_req);
			if (NullUtils.isEqual(auth.getUsername(), "googlesso")) {
				GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory).setAudience(Collections.singletonList(googleSsoKey)).build();
				try {
					GoogleIdToken idToken = verifier.verify(auth.getPassword());
					if (idToken != null) {
						GoogleIdToken.Payload payload = idToken.getPayload();
						String email = payload.getEmail();
						authCode = Globals.dao.getAuthCodeForEmail(email);
					}
				}
				catch (Exception _e) {
					logger.error("Failed to validate google auth token", _e);
  				}
			}
			else
				authCode = Globals.dao.authenticateAccount(auth.getUsername(), auth.getPassword());
		}
		DaoEntity rep = new DaoEntity("auth_code", authCode);
		if (isPath(_req, 0, "bin"))
			zipBsonResponse(_rep, rep);
		else
			jsonResponse(_rep, rep);
	}
}
