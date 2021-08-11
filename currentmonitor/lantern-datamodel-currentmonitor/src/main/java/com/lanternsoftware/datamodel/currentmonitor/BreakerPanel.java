package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.IIdentical;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Objects;

@DBSerializable
public class BreakerPanel implements IIdentical<BreakerPanel> {
	private int accountId;
	private String name;
	private int index;
	private int spaces;
	private int meter;

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int _accountId) {
		accountId = _accountId;
	}

	public String getName() {
		return name;
	}

	public void setName(String _name) {
		name = _name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int _index) {
		index = _index;
	}

	public int getSpaces() {
		return spaces;
	}

	public void setSpaces(int _spaces) {
		spaces = _spaces;
	}

	public int getMeter() {
		return meter;
	}

	public void setMeter(int _meter) {
		meter = _meter;
	}

	@Override
	public boolean equals(Object _o) {
		if (this == _o) return true;
		if (_o == null || getClass() != _o.getClass()) return false;
		BreakerPanel that = (BreakerPanel) _o;
		return accountId == that.accountId && index == that.index;
	}

	@Override
	public boolean isIdentical(BreakerPanel _o) {
		if (this == _o) return true;
		return accountId == _o.accountId && index == _o.index && spaces == _o.spaces && meter == _o.meter && Objects.equals(name, _o.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(accountId, index);
	}
}
