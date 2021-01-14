package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;

import java.util.List;

@DBSerializable(autogen = false)
public class Account {
	@PrimaryKey	private int id;
	private String username;
	private String password;
	private List<Integer> auxiliaryAccountIds;

	public int getId() {
		return id;
	}

	public void setId(int _id) {
		id = _id;
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

	public List<Integer> getAuxiliaryAccountIds() {
		return auxiliaryAccountIds;
	}

	public void setAuxiliaryAccountIds(List<Integer> _auxiliaryAccountIds) {
		auxiliaryAccountIds = _auxiliaryAccountIds;
	}
}
