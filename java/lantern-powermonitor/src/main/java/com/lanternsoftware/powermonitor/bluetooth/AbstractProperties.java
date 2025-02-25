package com.lanternsoftware.powermonitor.bluetooth;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractProperties implements Properties {
    protected final String interfaceName;
    protected final Map<String, Variant<?>> properties = new HashMap<>();

    public AbstractProperties(Class<? extends DBusInterface> _bleClass) {
        interfaceName = _bleClass.getCanonicalName();
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public abstract DBusPath getPath();

    @SuppressWarnings("unchecked")
    @Override
    public <A> A Get(String _interface, String _propertyName) {
        if (NullUtils.isNotEqual(_interface, interfaceName))
            return null;
        Variant<?> var = properties.get(_propertyName);
        try {
            return (A) var.getValue();
        }
        catch (ClassCastException _e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A> void Set(String _interface, String _propertyName, A _value) {
        if ((_value == null) || NullUtils.isNotEqual(_interface, interfaceName))
            return;
        properties.put(_propertyName, new Variant(_value));
    }

    @Override
    public Map<String, Variant<?>> GetAll(String _interfaceName) {
        if (NullUtils.isNotEqual(_interfaceName, getInterfaceName()))
            return new HashMap<>();
        return getProperties();
    }

    public Map<String, Variant<?>> getProperties() {
        return properties;
    }

    public List<AbstractProperties> getAllObjects() {
        List<AbstractProperties> objects = new ArrayList<>();
        getAllObjects(objects);
        return objects;
    }

    public void getAllObjects(List<AbstractProperties> _objects) {
        _objects.add(this);
        for (AbstractProperties o : CollectionUtils.makeNotNull(getChildObjects())) {
            o.getAllObjects(_objects);
        }
    }

    public List<? extends AbstractProperties> getChildObjects() {
        return null;
    }

    public Map<DBusPath, Map<String, Map<String, Variant<?>>>> getAllManagedObjects() {
        return getAllManagedObjects(getAllObjects());
    }

    public static Map<DBusPath, Map<String, Map<String, Variant<?>>>> getAllManagedObjects(List<AbstractProperties> _objects) {
        Map<DBusPath, Map<String, Map<String, Variant<?>>>> objects = new HashMap<>();
        for (AbstractProperties o : _objects) {
            objects.put(o.getPath(), CollectionUtils.asHashMap(o.getInterfaceName(), o.getProperties()));
        }
        return objects;
    }
}
