package com.lanternsoftware.currentmonitor.servlet;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.util.DateUtils;
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

@WebServlet("/auth/*")
public class AuthServlet extends CMServlet {
	private static final NetHttpTransport transport = new NetHttpTransport();
	private static final JacksonFactory jsonFactory = new JacksonFactory();
	private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);
	private static final String googleClientId;
	private static final String googleClientSecret;
	static {
		DaoEntity google = DaoSerializer.parse(ResourceLoader.loadFileAsString(LanternFiles.OPS_PATH + "google_sso.txt"));
		googleClientId = DaoSerializer.getString(google, "id");
		googleClientSecret = DaoSerializer.getString(google, "secret");
	}

	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		String authCode = _req.getHeader("auth_code");
		if (NullUtils.isEmpty(authCode)) {
			BasicAuth auth = new BasicAuth(_req);
			if (NullUtils.isEqual(auth.getUsername(), "googlesso")) {
				logger.info("Attempting google SSO");
				try {
					GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(transport, jsonFactory, "https://oauth2.googleapis.com/token", googleClientId, googleClientSecret, auth.getPassword(), "").execute();
					if (tokenResponse != null) {
						GoogleIdToken idToken = tokenResponse.parseIdToken();
						if (idToken != null) {
							logger.info("Successfully received google id token");
							authCode = Globals.dao.getAuthCodeForEmail(idToken.getPayload().getEmail(), DateUtils.fromTimeZoneId(_req.getHeader("timezone")));
							logger.info("Auth code for google user is valid: " + (authCode != null));
						}
					}
				} catch (Exception _e) {
					logger.error("Failed to validate google auth code", _e);
				}
			} else
				authCode = Globals.dao.authenticateAccount(auth.getUsername(), auth.getPassword());
		}
		DaoEntity rep = new DaoEntity("auth_code", authCode).and("timezone", Globals.dao.getTimeZoneForAccount(authCode));
		if (isPath(_req, 0, "bin"))
			zipBsonResponse(_rep, rep);
		else
			jsonResponse(_rep, rep);
	}
}
