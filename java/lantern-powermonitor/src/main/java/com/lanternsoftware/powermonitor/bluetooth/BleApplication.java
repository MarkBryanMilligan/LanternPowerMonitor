package com.lanternsoftware.powermonitor.bluetooth;

import com.lanternsoftware.util.CollectionUtils;
import org.bluez.GattApplication1;
import org.bluez.GattManager1;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.interfaces.ObjectManager;
import org.freedesktop.dbus.types.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BleApplication implements GattApplication1, ObjectManager {
    private static final Logger LOG = LoggerFactory.getLogger(BleApplication.class);

    private final String name;
    private final DBusPath appPath;
    private final GattManager1 appManager;
    private final List<BleService> services;
    private final BleAdvertisement advertisement;

    public BleApplication(String _name, String _advertisedName, BleService... _services) {
        this(_name, _advertisedName, CollectionUtils.asArrayList(_services));
    }

    public BleApplication(String _name, String _advertisedName, List<BleService> _services) {
        name = _name;
        appPath = new DBusPath(BleHelper.applicationPath(_name));
        appManager = BleHelper.getRemoteObject(BleHelper.getAdapter().getDbusPath(), GattManager1.class);
        services = _services;
        advertisement = new BleAdvertisement(_advertisedName, this);
        List<AbstractProperties> objects = getManagedObjects();
        BleHelper.unExportObject(this);
        objects.forEach(BleHelper::unExportObject);
        BleHelper.exportObject(this);
        objects.forEach(BleHelper::exportObject);
    }

    public List<AbstractProperties> getManagedObjects() {
        return CollectionUtils.aggregate(services, AbstractProperties::getAllObjects);
    }

    public String getName() {
        return name;
    }

    public List<BleService> getServices() {
        return services;
    }

    @Override
    public Map<DBusPath, Map<String, Map<String, Variant<?>>>> GetManagedObjects() {
        return AbstractProperties.getAllManagedObjects(getManagedObjects());
    }

    public void start() {
        try {
            appManager.RegisterApplication(appPath, new HashMap<>());
            advertisement.start();
        }
        catch (Exception _e) {
            LOG.error("Failed to register application", _e);
            _e.printStackTrace();
        }
    }

    public void stop() {
        try {
            advertisement.stop();
            appManager.UnregisterApplication(appPath);
            BleHelper.unExportObject(this);
            getManagedObjects().forEach(BleHelper::unExportObject);
            BleHelper.connection.disconnect();
            LOG.info("Bluetooth service and advertisement stopped");
        }
        catch (Exception _e) {
            LOG.error("Failed to unregister application", _e);
        }
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public String getObjectPath() {
        return appPath.getPath();
    }
}
