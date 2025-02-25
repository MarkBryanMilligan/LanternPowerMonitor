package com.lanternsoftware.powermonitor.datamodel;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@DBSerializable
public class HubCommand {
	@PrimaryKey	private String id;
	private int accountId;
	private int hub;
	private Date created;
	private HubConfigCharacteristic characteristic;
	private byte[] data;

	public HubCommand() {
	}

	public HubCommand(int _accountId, HubConfigCharacteristic _characteristic, byte[] _data) {
		accountId = _accountId;
		created = new Date();
		characteristic = _characteristic;
		data = _data;
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

	public int getHub() {
		return hub;
	}

	public void setHub(int _hub) {
		hub = _hub;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date _created) {
		created = _created;
	}

	public HubConfigCharacteristic getCharacteristic() {
		return characteristic;
	}

	public void setCharacteristic(HubConfigCharacteristic _characteristic) {
		characteristic = _characteristic;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] _data) {
		data = _data;
	}

	public List<HubCommand> forAllHubs(BreakerConfig _config) {
		return CollectionUtils.transform(_config.getBreakerHubs(), _h->forHub(_h.getHub()));
	}

	public HubCommand forHub(int _hub) {
		HubCommand c = new HubCommand();
		c.setAccountId(accountId);
		c.setHub(_hub);
		c.setCreated(created);
		c.setCharacteristic(characteristic);
		c.setData(data);
		return c;
	}

	@Override
	public boolean equals(Object _o) {
		if (this == _o) return true;
		if (_o == null || getClass() != _o.getClass()) return false;
		HubCommand that = (HubCommand) _o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
