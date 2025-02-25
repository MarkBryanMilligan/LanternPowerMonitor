package com.lanternsoftware.util.cloudservices.google;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.http.HttpFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GoogleSSO {
	private static final Logger logger = LoggerFactory.getLogger(GoogleSSO.class);
	private final String googleClientId;
	private final String googleClientSecret;

	public GoogleSSO(String _credentialsPath) {
		DaoEntity google = DaoSerializer.parse(ResourceLoader.loadFileAsString(_credentialsPath));
		googleClientId = DaoSerializer.getString(google, "id");
		googleClientSecret = DaoSerializer.getString(google, "secret");
	}

	public String signin(String _code) {
		HttpPost post = new HttpPost("https://oauth2.googleapis.com/token");
		List<NameValuePair> payload = new ArrayList<>();
		payload.add(new BasicNameValuePair("grant_type", "authorization_code"));
		payload.add(new BasicNameValuePair("code", _code));
		payload.add(new BasicNameValuePair("redirect_uri", "https://lanternsoftware.com/console"));
		payload.add(new BasicNameValuePair("client_id", googleClientId));
		payload.add(new BasicNameValuePair("client_secret", googleClientSecret));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(payload, StandardCharsets.UTF_8);
		entity.setContentType("application/x-www-form-urlencoded");
		post.setEntity(entity);
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
		String idToken = DaoSerializer.getString(DaoSerializer.parse(HttpFactory.pool().executeToString(post)), "id_token");
		if (NullUtils.isNotEmpty(idToken)) {
			try {
				DecodedJWT jwt = JWT.decode(idToken);
				return DaoSerializer.getString(DaoSerializer.parse(NullUtils.base64ToString(jwt.getPayload())), "email");
			} catch (Exception _e) {
				logger.error("Failed to validate google auth code", _e);
				return null;
			}
		}
		logger.error("Failed to validate google auth code");
		return null;
	}
}
