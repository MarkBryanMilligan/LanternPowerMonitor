package com.lanternsoftware.currentmonitor.bluetooth;

import com.lanternsoftware.datamodel.currentmonitor.CharacteristicFlag;
import com.lanternsoftware.util.CollectionUtils;
import org.bluez.GattCharacteristic1;
import org.bluez.datatypes.TwoTuple;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.types.UInt16;
import org.freedesktop.dbus.types.Variant;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BleCharacteristic extends AbstractProperties implements GattCharacteristic1 {
    private final String charName;
    private final DBusPath charPath;
    private final UUID uuid;
    private final List<BleDescriptor> descriptors;
    private BleCharacteristicListener listener;

    public BleCharacteristic(String _serviceName, UUID _uuid, String _characteristicName, Collection<CharacteristicFlag> _flags) {
        this(_serviceName, _uuid, _characteristicName, _flags, null);
    }

    public BleCharacteristic(String _serviceName, UUID _uuid, String _characteristicName, Collection<CharacteristicFlag> _flags, List<BleDescriptor> _descriptors) {
        super(GattCharacteristic1.class);
        charName = _characteristicName;
        charPath = new DBusPath(BleHelper.characteristicPath(_serviceName, _characteristicName));
        uuid = _uuid;
        descriptors = _descriptors;
        properties.put("Service", new Variant<>(new DBusPath(BleHelper.servicePath(_serviceName))));
        if (uuid != null)
            properties.put("UUID", new Variant<>(uuid.toString()));
        if (CollectionUtils.isNotEmpty(_flags))
            properties.put("Flags", new Variant<>(CharacteristicFlag.toArray(_flags)));
        if (CollectionUtils.isNotEmpty(descriptors))
            properties.put("Descriptors", new Variant<>(BleHelper.toPaths(descriptors)));
    }

    public void setListener(BleCharacteristicListener _listener) {
        listener = _listener;
    }

    @Override
    public List<? extends AbstractProperties> getChildObjects() {
        return descriptors;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    public DBusPath getPath() {
        return charPath;
    }

    @Override
    public String getObjectPath() {
        return charPath.getPath();
    }

    @Override
    public byte[] ReadValue(Map<String, Variant<?>> _options) {
        if (listener == null)
            return null;
        int offset = 0;
        Variant<?> voffset = _options.get("offset");
        if (voffset != null) {
            if (voffset.getValue() instanceof UInt16)
                offset = ((UInt16)voffset.getValue()).intValue();
        }
        byte[] ret = listener.read(charName);
        if (ret == null)
            return null;
        return offset > 0?Arrays.copyOfRange(ret, offset, ret.length):ret;
    }

    @Override
    public void WriteValue(byte[] _bytes, Map<String, Variant<?>> _map) {
        if (listener != null)
            listener.write(charName, _bytes);
    }

    @Override
    public TwoTuple<FileDescriptor, UInt16> AcquireWrite(Map<String, Variant<?>> _map) {
        return null;
    }

    @Override
    public TwoTuple<FileDescriptor, UInt16> AcquireNotify(Map<String, Variant<?>> _map) {
        return null;
    }

    @Override
    public void StartNotify() {

    }

    @Override
    public void StopNotify() {

    }

    @Override
    public void Confirm() {

    }

    public UUID getUuid() {
        return uuid;
    }

    public List<BleDescriptor> getDescriptors() {
        return descriptors;
    }
}
