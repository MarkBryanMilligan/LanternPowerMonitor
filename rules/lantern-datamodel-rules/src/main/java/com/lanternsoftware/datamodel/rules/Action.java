package com.lanternsoftware.datamodel.rules;

import com.lanternsoftware.util.dao.annotations.DBSerializable;

@DBSerializable
public class Action {
	private ActionType type;
	private String description;
	private String destinationId;
	private double value;

	public ActionType getType() {
		return type;
	}

	public void setType(ActionType _type) {
		type = _type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String _description) {
		description = _description;
	}

	public String getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(String _destinationId) {
		destinationId = _destinationId;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double _value) {
		value = _value;
	}
}
