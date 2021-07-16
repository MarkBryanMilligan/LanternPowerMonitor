package com.lanternsoftware.datamodel.rules;

import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;

import java.util.Date;

@DBSerializable
public class FcmDevice {
	@PrimaryKey private String id;
	private int accountId;
	private String token;
	private String name;
	private Date posted;

	public FcmDevice() {
	}

	public FcmDevice(int _accountId, String _token, String _name, Date _posted) {
		accountId = _accountId;
		token = _token;
		name = _name;
		posted = _posted;
	}

	public String getId() {
		return id;
	}

	public void setId(String _id) {
		id = _id;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String _token) {
		token = _token;
	}

	public String getName() {
		return name;
	}

	public void setName(String _name) {
		name = _name;
	}

	public Date getPosted() {
		return posted;
	}

	public void setPosted(Date _posted) {
		posted = _posted;
	}
}
