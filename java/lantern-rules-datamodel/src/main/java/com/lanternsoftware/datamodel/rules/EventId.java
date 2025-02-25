package com.lanternsoftware.datamodel.rules;

import java.util.Objects;

public class EventId {
	private final EventType type;
	private final String sourceId;

	public EventId(EventType _type, String _sourceId) {
		type = _type;
		sourceId = _sourceId;
	}

	public EventType getType() {
		return type;
	}

	public String getSourceId() {
		return sourceId;
	}

	@Override
	public boolean equals(Object _o) {
		if (this == _o) return true;
		if (_o == null || getClass() != _o.getClass()) return false;
		EventId eventId = (EventId) _o;
		return type == eventId.type && Objects.equals(sourceId, eventId.sourceId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, sourceId);
	}
}
