package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class SignupResponse {
	private String error;
	private String authCode;

	public SignupResponse() {
	}

	public static SignupResponse error(String _error) {
		SignupResponse response = new SignupResponse();
		response.setError(_error);
		return response;
	}

	public static SignupResponse success(String _authCode) {
		SignupResponse response = new SignupResponse();
		response.setAuthCode(_authCode);
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

	public boolean isSuccess() {
		return NullUtils.isEmpty(error) && NullUtils.isNotEmpty(authCode);
	}
}
