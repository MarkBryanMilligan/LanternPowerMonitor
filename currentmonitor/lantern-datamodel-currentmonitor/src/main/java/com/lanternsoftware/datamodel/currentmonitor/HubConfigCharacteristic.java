package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.NullUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

public enum HubConfigCharacteristic {
	WifiCredentials(2, CharacteristicFlag.WRITE),
	AuthCode(3, CharacteristicFlag.WRITE),
	HubIndex(4, CharacteristicFlag.READ, CharacteristicFlag.WRITE),
	Restart(5, CharacteristicFlag.WRITE),
	Reboot(6, CharacteristicFlag.WRITE),
	AccountId(7, CharacteristicFlag.READ),
	NetworkState(8, CharacteristicFlag.READ),
	Flash(9, CharacteristicFlag.WRITE),
	Host(10, CharacteristicFlag.WRITE),
	Log(11, CharacteristicFlag.READ),
	NetworkDetails(12, CharacteristicFlag.READ),
	Shutdown(13, CharacteristicFlag.WRITE),
	Version(14, CharacteristicFlag.READ),
	Update(15, CharacteristicFlag.WRITE),
	ReloadConfig(16, CharacteristicFlag.WRITE);

	public final int idx;
	public final UUID uuid;
	public final EnumSet<CharacteristicFlag> flags;

	HubConfigCharacteristic(int _idx, CharacteristicFlag... _flags) {
		idx = _idx;
		uuid = HubConfigService.uuidFormat.format(_idx);
		flags = EnumSet.copyOf(Arrays.asList(_flags));
	}

	public int getIdx() {
		return idx;
	}

	public UUID getUUID() {
		return uuid;
	}

	public EnumSet<CharacteristicFlag> getFlags() {
		return flags;
	}

	public boolean isChar(String _char) {
		return NullUtils.isEqual(name(), _char);
	}

	public static HubConfigCharacteristic fromUUID(UUID _uuid) {
		for (HubConfigCharacteristic c : values()) {
			if (c.uuid.equals(_uuid))
				return c;
		}
		return null;
	}
}
