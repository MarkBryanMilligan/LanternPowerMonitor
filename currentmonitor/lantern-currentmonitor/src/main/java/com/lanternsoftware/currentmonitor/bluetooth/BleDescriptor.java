package com.lanternsoftware.currentmonitor.bluetooth;

import org.bluez.GattDescriptor1;
import org.freedesktop.dbus.DBusPath;

import java.util.UUID;

public class BleDescriptor extends AbstractProperties {
    private final DBusPath charPath;
    private final UUID uuid;

    public BleDescriptor(String _serviceName, String _characteristicName, String _descriptorName, UUID _uuid) {
        super(GattDescriptor1.class);
        charPath = new DBusPath(BleHelper.descriptorPath(_serviceName, _characteristicName, _descriptorName));
        uuid = _uuid;
    }

    @Override
    public DBusPath getPath() {
        return charPath;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public String getObjectPath() {
        return charPath.getPath();
    }
}
