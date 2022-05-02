package com.lanternsoftware.util.cloudservices.google;

import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class FirebaseCredentials {
	private String type;
	private String projectId;
	private String privateKeyId;
	private String privateKey;
	private String clientEmail;
	private String clientId;
	private String authUri;
	private String tokenUri;
	private String authProviderX509CertUrl;
	private String clientX509CertUrl;

	public String getType() {
		return type;
	}

	public void setType(String _type) {
		type = _type;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String _projectId) {
		projectId = _projectId;
	}

	public String getPrivateKeyId() {
		return privateKeyId;
	}

	public void setPrivateKeyId(String _privateKeyId) {
		privateKeyId = _privateKeyId;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String _privateKey) {
		privateKey = _privateKey;
	}

	public String getClientEmail() {
		return clientEmail;
	}

	public void setClientEmail(String _clientEmail) {
		clientEmail = _clientEmail;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String _clientId) {
		clientId = _clientId;
	}

	public String getAuthUri() {
		return authUri;
	}

	public void setAuthUri(String _authUri) {
		authUri = _authUri;
	}

	public String getTokenUri() {
		return tokenUri;
	}

	public void setTokenUri(String _tokenUri) {
		tokenUri = _tokenUri;
	}

	public String getAuthProviderX509CertUrl() {
		return authProviderX509CertUrl;
	}

	public void setAuthProviderX509CertUrl(String _authProviderX509CertUrl) {
		authProviderX509CertUrl = _authProviderX509CertUrl;
	}

	public String getClientX509CertUrl() {
		return clientX509CertUrl;
	}

	public void setClientX509CertUrl(String _clientX509CertUrl) {
		clientX509CertUrl = _clientX509CertUrl;
	}
}
