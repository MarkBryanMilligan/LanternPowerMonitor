package com.lanternsoftware.powermonitor.datamodel;

import com.lanternsoftware.util.CollectionUtils;

import java.util.Collection;

public enum CharacteristicFlag {
	BROADCAST("broadcast"),
	READ("read"),
	WRITE_WITHOUT_RESPONSE("write-without-response"),
	WRITE("write"),
	NOTIFY("notify"),
	INDICATE("indicate"),
	AUTHENTICATED_SIGNED_WRITES("authenticated-signed-writes"),
	RELIABLE_WRITE("reliable-write"),
	WRITABLE_AUXILIARIES("writable-auxiliaries"),
	ENCRYPT_READ("encrypt-read"),
	ENCRYPT_WRITE("encrypt-write"),
	ENCRYPT_AUTHENTICATED_READ("encrypt-authenticated-read"),
	ENCRYPT_AUTHENTICATED_WRITE("encrypt-authenticated-write");

	CharacteristicFlag(String _value) {
		value = _value;
	}

	public final String value;

	public static String[] toArray(Collection<CharacteristicFlag> _flags) {
		return CollectionUtils.transform(_flags, _c->_c.value).toArray(new String[0]);
	}
}
