package com.lanternsoftware.util.cloudservices.google;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.http.HttpFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FirebaseHelper {
	private static final Logger LOG = LoggerFactory.getLogger(FirebaseHelper.class);
	private static final String FCM_SEND_URL = "https://fcm.googleapis.com/v1/projects/%s/messages:send";
	private static final List<String> SCOPES = List.of("https://www.googleapis.com/auth/firebase.database", "https://www.googleapis.com/auth/userinfo.email", "https://www.googleapis.com/auth/identitytoolkit", "https://www.googleapis.com/auth/devstorage.full_control", "https://www.googleapis.com/auth/cloud-platform", "https://www.googleapis.com/auth/datastore");

	private final FirebaseCredentials credentials;
	private final RSAPrivateKey privateKey;
	private final String fcmSendUrl;
	private String accessToken;
	private Date validUntil;

	public FirebaseHelper(String _credentialsPath) {
		this(DaoSerializer.parse(ResourceLoader.loadFileAsString(_credentialsPath), FirebaseCredentials.class));
	}

	public FirebaseHelper(FirebaseCredentials _credentials) {
		credentials = _credentials;
		if (credentials != null) {
			privateKey = fromPEM(credentials.getPrivateKey());
			fcmSendUrl = String.format(FCM_SEND_URL, credentials.getProjectId());
		}
		else {
			LOG.error("Failed to load FCM credentials");
			privateKey = null;
			fcmSendUrl = null;
		}
	}

	private RSAPrivateKey fromPEM(String _pem) {
		try {
			String pem = _pem.replaceAll("(-+BEGIN PRIVATE KEY-+|-+END PRIVATE KEY-+|\\r|\\n)", "");
			byte[] encoded = Base64.decodeBase64(pem);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			return (RSAPrivateKey)keyFactory.generatePrivate(keySpec);
		}
		catch (Exception _e) {
			LOG.error("Failed to generate RSA private key", _e);
			return null;
		}
	}

	private boolean validateAccessToken() {
		if (isTokenValid())
			return true;
		Date now = new Date();
		String assertion = JWT.create().withKeyId(credentials.getPrivateKeyId()).withIssuer(credentials.getClientEmail()).withIssuedAt(new Date()).withExpiresAt(DateUtils.addHours(now, 1)).withClaim("scope", CollectionUtils.delimit(SCOPES, " ")).withAudience(credentials.getTokenUri()).sign(Algorithm.RSA256(null, privateKey));

		HttpPost post = new HttpPost(credentials.getTokenUri());
		List<NameValuePair> payload = new ArrayList<>();
		payload.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer"));
		payload.add(new BasicNameValuePair("assertion", assertion));
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(payload, StandardCharsets.UTF_8);
		entity.setContentType("application/x-www-form-urlencoded");
		post.setEntity(entity);
		DaoEntity rep = DaoSerializer.parse(HttpFactory.pool().executeToString(post));
		if (rep == null)
			return false;
		accessToken = DaoSerializer.getString(rep, "access_token");
		validUntil = DateUtils.secondsFromNow(DaoSerializer.getInteger(rep, "expires_in")-10);
		return isTokenValid();
	}

	private boolean isTokenValid() {
		return NullUtils.isNotEmpty(accessToken) && (validUntil != null) && new Date().before(validUntil);
	}

	public boolean sendMessage(String _deviceToken, DaoEntity _payload) {
		if (!validateAccessToken()) {
			LOG.error("Failed to get a valid access token for Firebase, not sending message");
			return false;
		}
		DaoEntity msg = new DaoEntity("message", new DaoEntity("token", _deviceToken).and("data", _payload).and("android", new DaoEntity("priority", "high").and("direct_boot_ok", true)));
		HttpPost post = new HttpPost(fcmSendUrl);
		post.addHeader("X-GOOG-API-FORMAT-VERSION", "2");
		post.addHeader("X-Firebase-Client", "fire-admin-java/8.0.0");
		post.addHeader("Authorization", "Bearer " + accessToken);
		post.setEntity(new StringEntity(DaoSerializer.toJson(msg), StandardCharsets.UTF_8));
		return NullUtils.isNotEmpty(HttpFactory.pool().executeToString(post));
	}
}
