package com.lanternsoftware.datamodel.rules;

import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;

import java.util.Date;

@DBSerializable
public class Event {
	@PrimaryKey	private String id;
	private int accountId;
	private EventType type;
	private Date time;
	private String eventDescription;
	private String sourceId;
	private double value;

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

	public EventType getType() {
		return type;
	}

	public void setType(EventType _type) {
		type = _type;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date _time) {
		time = _time;
	}

	public String getEventDescription() {
		return eventDescription;
	}

	public void setEventDescription(String _eventDescription) {
		eventDescription = _eventDescription;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String _sourceId) {
		sourceId = _sourceId;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double _value) {
		value = _value;
	}
}
