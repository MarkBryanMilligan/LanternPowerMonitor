package com.lanternsoftware.util.dao.auth;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.ArrayList;
import java.util.List;

@DBSerializable(autogen = false)
public class AuthCode {
	private int accountId;
	private List<Integer> auxiliaryAccountIds;

	public AuthCode() {
	}

	public AuthCode(int _accountId, List<Integer> _auxiliaryAccountIds) {
		accountId = _accountId;
		auxiliaryAccountIds = _auxiliaryAccountIds;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public List<Integer> getAuxiliaryAccountIds() {
		return auxiliaryAccountIds;
	}

	public void setAuxiliaryAccountIds(List<Integer> _auxiliaryAccountIds) {
		auxiliaryAccountIds = _auxiliaryAccountIds;
	}

	public List<Integer> getAllAccountIds() {
		List<Integer> ids = new ArrayList<>();
		ids.add(accountId);
		if (auxiliaryAccountIds != null)
			ids.addAll(auxiliaryAccountIds);
		return ids;
	}

	public boolean isAuthorized(int _accountId) {
		return accountId == _accountId || CollectionUtils.contains(auxiliaryAccountIds, _accountId);
	}
}
