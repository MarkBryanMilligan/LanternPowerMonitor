package com.lanternsoftware.currentmonitor.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.external.LanternFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimeZone;

public class GoogleAuthHelper {
	private static final Logger logger = LoggerFactory.getLogger(GoogleAuthHelper.class);
	private static final NetHttpTransport transport = new NetHttpTransport();
	private static final String googleClientId;
	private static final String googleClientSecret;
	static {
		DaoEntity google = DaoSerializer.parse(ResourceLoader.loadFileAsString(LanternFiles.CONFIG_PATH + "google_sso.txt"));
		googleClientId = DaoSerializer.getString(google, "id");
		googleClientSecret = DaoSerializer.getString(google, "secret");
	}

	public static String signin(String _code, TimeZone _tz) {
		try {
			GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(transport, new GsonFactory(), "https://oauth2.googleapis.com/token", googleClientId, googleClientSecret, _code, "https://lanternsoftware.com/console").execute();
			if (tokenResponse != null) {
				GoogleIdToken idToken = tokenResponse.parseIdToken();
				if (idToken != null)
					return Globals.dao.getAuthCodeForEmail(idToken.getPayload().getEmail(), _tz);
			}
		} catch (Exception _e) {
			logger.error("Failed to validate google auth code", _e);
		}
		return null;
	}
}
