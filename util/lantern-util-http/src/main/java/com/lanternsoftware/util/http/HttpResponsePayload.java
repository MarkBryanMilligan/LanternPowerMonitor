package com.lanternsoftware.util.http;

import com.lanternsoftware.util.NullUtils;

public class HttpResponsePayload {
	private final int status;
	private final byte[] payload;

	public HttpResponsePayload(int _status, byte[] _payload) {
		status = _status;
		payload = _payload;
	}

	public int getStatus() {
		return status;
	}

	public byte[] getPayload() {
		return payload;
	}

	public boolean isSuccess() {
		return (status >= 200) && (status < 300);
	}

	public String asString() {
		return NullUtils.toString(payload);
	}
}
