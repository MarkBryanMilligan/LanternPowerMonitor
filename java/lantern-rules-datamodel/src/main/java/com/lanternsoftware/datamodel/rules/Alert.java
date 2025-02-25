package com.lanternsoftware.datamodel.rules;

import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class Alert {
	private String message;

	public Alert() {
	}

	public Alert(String _message) {
		message = _message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String _message) {
		message = _message;
	}
}
