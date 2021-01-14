package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.NoCommandResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ControllerCapabilitiesResponse extends NoCommandResponseMessage {
	private static final Logger logger = LoggerFactory.getLogger(ControllerCapabilitiesResponse.class);

	private String serialAPIVersion;
	private int manufacturerId;
	private int deviceType;
	private int deviceId;
	private Set<CommandClass> supportedCommandClasses;

	public ControllerCapabilitiesResponse() {
		super(ControllerMessageType.GetCapabilities);
	}

	@Override
	public void fromPayload(byte[] _payload) {
		serialAPIVersion = String.format("%d.%d", _payload[4], _payload[5]);
		manufacturerId = getShort(_payload, 6);
		deviceType = getShort(_payload, 8);
		deviceId = getShort(_payload, 10);
		supportedCommandClasses = new HashSet<>();
		for (int by = 12; by < _payload.length-1; by++) {
			for (int bi = 0; bi < 8; bi++) {
				if ((_payload[by] & (0x01 << bi)) != 0) {
					byte commandClassByte = (byte) (((by - 12) << 3) + bi + 1);
					CommandClass commandClass = CommandClass.fromByte(commandClassByte);
					if (commandClass != CommandClass.NO_OPERATION) {
						logger.debug("Supports command class: {}", commandClass.label);
						supportedCommandClasses.add(commandClass);
					} else {
						logger.debug("Supports unknown command class: {}", commandClassByte);
					}
				}
			}
		}
	}

	private int getShort(byte[] _data, int _offset) {
		return (toShort(_data[_offset]) << 8) | toShort(_data[_offset + 1]);
	}

	private int toShort(byte _bt) {
		return _bt & 0xFF;
	}

	public String getSerialAPIVersion() {
		return serialAPIVersion;
	}

	public int getManufacturerId() {
		return manufacturerId;
	}

	public int getDeviceType() {
		return deviceType;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public Set<CommandClass> getSupportedCommandClasses() {
		return supportedCommandClasses;
	}
}
