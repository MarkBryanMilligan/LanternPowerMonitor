package com.lanternsoftware.currentmonitor.bluetooth;

import com.lanternsoftware.util.CollectionUtils;
import org.bluez.GattService1;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.types.Variant;

import java.util.List;
import java.util.UUID;


public class BleService extends AbstractProperties implements GattService1 {
	private final DBusPath servicePath;
	private final UUID uuid;
	private final List<BleCharacteristic> characteristics;

	public BleService(String _serviceName, UUID _uuid, List<BleCharacteristic> _characteristics) {
		super(GattService1.class);
		servicePath = new DBusPath(BleHelper.servicePath(_serviceName));
		uuid = _uuid;
		characteristics = _characteristics;
		if (uuid != null)
			properties.put("UUID", new Variant<>(uuid.toString()));
		properties.put("Primary", new Variant<>(Boolean.TRUE));
		if (CollectionUtils.isNotEmpty(characteristics))
			properties.put("Characteristics", new Variant<>(BleHelper.toPaths(characteristics)));
	}

	public DBusPath getPath() {
		return servicePath;
	}

	@Override
	public boolean isRemote() {
		return false;
	}

	@Override
	public String getObjectPath() {
		return getPath().getPath();
	}

	@Override
	public List<? extends AbstractProperties> getChildObjects() {
		return characteristics;
	}

	public UUID getUuid() {
		return uuid;
	}

	public List<BleCharacteristic> getCharacteristics() {
		return characteristics;
	}
}
