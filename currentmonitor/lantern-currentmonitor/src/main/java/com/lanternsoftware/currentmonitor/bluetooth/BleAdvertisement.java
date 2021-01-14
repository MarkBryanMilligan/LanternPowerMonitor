package com.lanternsoftware.currentmonitor.bluetooth;

import com.lanternsoftware.util.CollectionUtils;
import org.bluez.LEAdvertisement1;
import org.bluez.LEAdvertisingManager1;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class BleAdvertisement extends AbstractProperties implements LEAdvertisement1, Properties {
    private static final Logger LOG = LoggerFactory.getLogger(BleAdvertisement.class);

    private final LEAdvertisingManager1 advertiser;
    private final DBusPath advertisementPath;

    public BleAdvertisement(String _name, BleApplication _app) {
        super(LEAdvertisement1.class);
        String[] serviceUUIDs = CollectionUtils.transform(_app.getServices(), _s->_s.getUuid().toString()).toArray(new String[0]);
        advertisementPath = new DBusPath(BleHelper.advertismentPath(_app.getName()));
        advertiser = BleHelper.getRemoteObject(BleHelper.getAdapter().getDbusPath(), LEAdvertisingManager1.class);
        properties.put("Type", new Variant<>("peripheral"));
        properties.put("ServiceUUIDs", new Variant<>(serviceUUIDs));
        properties.put("LocalName", new Variant<>(_name));
        properties.put("Includes", new Variant<>(new String[]{"tx-power"}));
        BleHelper.unExportObject(this);
        BleHelper.exportObject(this);
    }

    public void start() {
        try {
            advertiser.RegisterAdvertisement(advertisementPath, new HashMap<>());
        }
        catch (Exception _e) {
            LOG.error("Failed to register advertisement", _e);
        }
    }

    public void stop() {
        try {
            advertiser.UnregisterAdvertisement(advertisementPath);
        }
            catch (Exception _e) {
                LOG.error("Failed to unregister advertisement", _e);
        }
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public String getObjectPath() {
        return advertisementPath.getPath();
    }

    @Override
    public DBusPath getPath() {
        return advertisementPath;
    }

    @Override
    public void Release() {
    }
}
