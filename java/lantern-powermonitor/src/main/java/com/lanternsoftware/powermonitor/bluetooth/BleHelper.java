package com.lanternsoftware.powermonitor.bluetooth;

import com.github.hypfvieh.bluetooth.DeviceManager;
import com.github.hypfvieh.bluetooth.wrapper.BluetoothAdapter;
import com.lanternsoftware.util.CollectionUtils;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class BleHelper {
    private static final Logger LOG = LoggerFactory.getLogger(BleHelper.class);

    private static String basePath;
    private static final DeviceManager deviceManager;
    public static final DBusConnection connection;
    static {
        DeviceManager m = null;
        DBusConnection c = null;
        try {
            DeviceManager.createInstance(false);
            m = DeviceManager.getInstance();
            c = m.getDbusConnection();
        }
        catch (Exception _e) {
            LOG.error("Failed to get dbus connection", _e);
        }
        deviceManager = m;
        connection = c;
    }

    public static void setBasePath(String _basePath) {
        basePath = _basePath;
    }

    public static String getBasePath() {
        return basePath;
    }

    public static String advertismentPath(String _advertisementName) {
        return BleHelper.getBasePath() + "/advertisement/" + _advertisementName;
    }

    public static String applicationPath(String _appPath) {
        return BleHelper.getBasePath() + "/application/" + _appPath;
    }

    public static String servicePath(String _serviceName) {
        return BleHelper.getBasePath() + "/service/" + _serviceName;
    }

    public static String characteristicPath(String _serviceName, String _characteristicName) {
        return servicePath(_serviceName) + "/" + _characteristicName;
    }

    public static String descriptorPath(String _serviceName, String _characteristicName, String _descriptorPath) {
        return servicePath(_serviceName) + "/" + _characteristicName + "/" + _descriptorPath;
    }

    public static String[] toPaths(List<? extends AbstractProperties> _properties) {
        return CollectionUtils.transform(_properties, Properties::getObjectPath).toArray(new String[0]);
    }

    public static BluetoothAdapter getAdapter() {
        return (deviceManager != null)?deviceManager.getAdapter():null;
    }

    public static void requestBusName(String _name) {
        try {
            if (connection != null)
                connection.requestBusName(_name);
        }
        catch (Exception _e) {
            LOG.error("Failed to request bus name", _e);
        }
    }

    public static <T extends DBusInterface> T getRemoteObject(String _path, Class<T> _objClass) {
        try {
            return connection.getRemoteObject("org.bluez", _path, _objClass);
        } catch (DBusException _e) {
            LOG.error("Failed to get remote object", _e);
            return null;
        }
    }

    public static void exportObject(DBusInterface _object) {
        try {
            if (connection != null)
                connection.exportObject(_object.getObjectPath(), _object);
        }
        catch (Exception _e) {
            LOG.error("Failed to export object", _e);
        }
    }

    public static void unExportObject(DBusInterface _object) {
        if (_object != null)
            unExportObject(_object.getObjectPath());
    }

    public static void unExportObject(String _objectPath) {
        try {
            if (connection != null)
                connection.unExportObject(_objectPath);
        }
        catch (Exception _e) {
            LOG.error("Failed to unexport object", _e);
        }
    }
}
