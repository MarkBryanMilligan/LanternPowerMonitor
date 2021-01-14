package com.lanternsoftware.zwave.message.impl;

import com.lanternsoftware.zwave.message.CommandClass;
import com.lanternsoftware.zwave.message.ControllerMessageType;
import com.lanternsoftware.zwave.message.RequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ApplicationUpdateRequest extends RequestMessage {
	private static final Logger logger = LoggerFactory.getLogger(ApplicationUpdateRequest.class);

	private List<CommandClass> commandClasses;

	public ApplicationUpdateRequest() {
		super(ControllerMessageType.ApplicationUpdate, CommandClass.NO_OPERATION, (byte) 0);
	}

	@Override
	public void fromPayload(byte[] _payload) {
		nodeId = _payload[5];
		if (_payload[4] == (byte) 0x84)
			logger.debug("Received node information for node: {}", nodeId);
		int length = _payload[6];
		commandClasses = new ArrayList<>();
		for (int i = 7; i < length + 7; i++) {
			if (_payload[i] != (byte) 0xef) {
				CommandClass commandClass = CommandClass.fromByte(_payload[i]);
				if (commandClass != CommandClass.NO_OPERATION) {
					logger.debug("Received command class: {} for node: {}", commandClass.name(), nodeId);
					commandClasses.add(commandClass);
				}
			}
		}
	}

	public List<CommandClass> getCommandClasses() {
		return commandClasses;
	}
}
