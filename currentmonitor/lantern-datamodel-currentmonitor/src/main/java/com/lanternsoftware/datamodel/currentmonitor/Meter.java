package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.IIdentical;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Objects;

@DBSerializable
public class Meter implements IIdentical<Meter> {
	private int accountId;
	private int index;
	private String name;

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int _index) {
		index = _index;
	}

	public String getName() {
		return name;
	}

	public void setName(String _name) {
		name = _name;
	}

	@Override
	public boolean equals(Object _o) {
		if (this == _o) return true;
		if (_o == null || getClass() != _o.getClass()) return false;
		Meter meter = (Meter) _o;
		return accountId == meter.accountId && index == meter.index;
	}

	@Override
	public boolean isIdentical(Meter _o) {
		if (this == _o) return true;
		if (_o == null) return false;
		return accountId == _o.accountId && index == _o.index && Objects.equals(name, _o.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(accountId, index);
	}
}
