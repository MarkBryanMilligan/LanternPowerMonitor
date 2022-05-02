package com.lanternsoftware.util.cloudservices.apple;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.http.HttpFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AppleSSO {
	private static final Logger LOG = LoggerFactory.getLogger(AppleSSO.class);
	private final Map<String, RSAPublicKey> publicKeys = new HashMap<>();
	private final String audience;

	public AppleSSO(String _credentialsPath) {
		audience = ResourceLoader.loadFileAsString(_credentialsPath).trim();
	}

	public String getEmailFromIdToken(String _idToken) {
		if (validatePublicKey()) {
			try {
				DecodedJWT jwt = JWT.decode(NullUtils.base64ToString(_idToken));
				String kid = jwt.getHeaderClaim("kid").asString();
				RSAPublicKey key = publicKeys.get(kid);
				if (key != null) {
					Algorithm algorithm = Algorithm.RSA256(key, null);
					JWTVerifier verifier = JWT.require(algorithm).withIssuer("https://appleid.apple.com").withAudience(audience).build();
					return verifier.verify(jwt).getClaim("email").asString().toLowerCase(Locale.ROOT);
				}
			} catch (Exception _e){
				LOG.error("Failed to verify Apple JWT token", _e);
			}
		}
		return null;
	}

	private synchronized boolean validatePublicKey() {
		if (!publicKeys.isEmpty())
			return true;
		DaoEntity resp = DaoSerializer.parse(HttpFactory.pool().executeToString(new HttpGet("https://appleid.apple.com/auth/keys")));
		for (DaoEntity key : DaoSerializer.getDaoEntityList(resp, "keys")) {
			try {
				KeyFactory fact = KeyFactory.getInstance("RSA");
				RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(1, Base64.decodeBase64(DaoSerializer.getString(key, "n"))), new BigInteger(1, Base64.decodeBase64(DaoSerializer.getString(key, "e"))));
				RSAPublicKey publicKey = (RSAPublicKey)fact.generatePublic(keySpec);
				if (publicKey != null)
					publicKeys.put(DaoSerializer.getString(key, "kid"), publicKey);
			} catch (Exception _e) {
				LOG.error("Failed to generate RSA public key", _e);
			}
		}
		return !publicKeys.isEmpty();
	}

}
