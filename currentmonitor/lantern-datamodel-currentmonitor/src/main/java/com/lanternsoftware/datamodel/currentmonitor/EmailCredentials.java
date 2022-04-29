package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class EmailCredentials {
	private EmailProvider provider;
	private String apiKey;
	private String apiSecret;
	private String emailFrom;
	private String serverUrlBase;

	public EmailProvider getProvider() {
		return provider;
	}

	public void setProvider(EmailProvider _provider) {
		provider = _provider;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String _apiKey) {
		apiKey = _apiKey;
	}

	public String getApiSecret() {
		return apiSecret;
	}

	public void setApiSecret(String _apiSecret) {
		apiSecret = _apiSecret;
	}

	public String getEmailFrom() {
		return emailFrom;
	}

	public void setEmailFrom(String _emailFrom) {
		emailFrom = _emailFrom;
	}

	public String getServerUrlBase() {
		return serverUrlBase;
	}

	public void setServerUrlBase(String _serverUrlBase) {
		serverUrlBase = _serverUrlBase;
	}
}
