package com.lanternsoftware.powermonitor.datamodel;


import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;

@DBSerializable
public class Sequence {
	@PrimaryKey
	private String id;
	private int sequence;

	public Sequence() {
	}

	public Sequence(int _sequence) {
		sequence = _sequence;
	}

	public String getId() {
		return id;
	}

	public void setId(String _id) {
		id = _id;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int _sequence) {
		sequence = _sequence;
	}
}
