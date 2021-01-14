package com.lanternsoftware.currentmonitor;


import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class MonitorConfig {
	private String host;
	private String authCode;
	private String username;
	private String password;
	private int hub;
	private boolean debug;
	private int connectTimeout;
	private int socketTimeout;
	private int updateInterval;

	public MonitorConfig() {
	}

	public MonitorConfig(int _hub, String _host) {
		hub = _hub;
		host = _host;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String _host) {
		host = _host;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String _authCode) {
		authCode = _authCode;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String _username) {
		username = _username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String _password) {
		password = _password;
	}

	public int getHub() {
		return hub;
	}

	public void setHub(int _hub) {
		hub = _hub;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean _debug) {
		debug = _debug;
	}

	public int getConnectTimeout() {
		return connectTimeout == 0?3000:connectTimeout;
	}

	public void setConnectTimeout(int _connectTimeout) {
		connectTimeout = _connectTimeout;
	}

	public int getSocketTimeout() {
		return socketTimeout == 0?5000:socketTimeout;
	}

	public void setSocketTimeout(int _socketTimeout) {
		socketTimeout = _socketTimeout;
	}

	public int getUpdateInterval() {
		return updateInterval == 0?300:updateInterval;
	}

	public void setUpdateInterval(int _updateInterval) {
		updateInterval = _updateInterval;
	}
}
