package com.lanternsoftware.powermonitor.datamodel;

import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class SignupResponse {
	private String error;
	private String authCode;
	private String timezone;

	public SignupResponse() {
	}

	public static SignupResponse error(String _error) {
		SignupResponse response = new SignupResponse();
		response.setError(_error);
		return response;
	}

	public static SignupResponse success(String _authCode, String _timezone) {
		SignupResponse response = new SignupResponse();
		response.setAuthCode(_authCode);
		response.setTimezone(_timezone);
		return response;
	}

	public String getError() {
		return error;
	}

	public void setError(String _error) {
		error = _error;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String _authCode) {
		authCode = _authCode;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String _timezone) {
		timezone = _timezone;
	}

	public boolean isSuccess() {
		return NullUtils.isEmpty(error) && NullUtils.isNotEmpty(authCode);
	}
}
