package com.lanternsoftware.datamodel.currentmonitor;

import java.util.UUID;

public class UUIDFormatter {
	private final String uuidPrefix;
	private final String uuidSuffix;

	public UUIDFormatter(String _uuid) {
		uuidPrefix = _uuid.substring(0,4);
		uuidSuffix = _uuid.substring(8);
	}

	public UUID format(int _idx) {
		return UUID.fromString(uuidPrefix + String.format("%04X", _idx) + uuidSuffix);
	}
}
