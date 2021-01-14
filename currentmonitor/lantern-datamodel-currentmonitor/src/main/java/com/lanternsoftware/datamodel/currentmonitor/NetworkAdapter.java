package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.CollectionUtils;

import java.util.Collection;
import java.util.EnumSet;

public enum NetworkAdapter {
	ETHERNET((byte)0x1),
	WIFI((byte)0x2);

	public final byte bt;

	NetworkAdapter(byte _bt) {
		bt = _bt;
	}

	public static NetworkAdapter fromByte(byte _bt) {
		for (NetworkAdapter a : values()) {
			if (a.bt == _bt)
				return a;
		}
		return null;
	}

	public static EnumSet<NetworkAdapter> fromMask(byte _bt) {
		EnumSet<NetworkAdapter> values = EnumSet.noneOf(NetworkAdapter.class);
		for (NetworkAdapter a : values()) {
			if ((a.bt & _bt) == a.bt)
				values.add(a);
		}
		return values;
	}

	public static byte toMask(Collection<NetworkAdapter> _adapters) {
		byte mask = 0;
		for (NetworkAdapter a : CollectionUtils.makeNotNull(_adapters)) {
			mask |= a.bt;
		}
		return mask;
	}
}
