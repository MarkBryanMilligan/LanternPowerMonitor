package com.lanternsoftware.zwave.message;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public abstract class MessageEngine {
	private static final Logger logger = LoggerFactory.getLogger(MessageEngine.class);

	private static final Map<String, Message> messages = new HashMap<>();
	private static final Map<Class<?>, List<IMessageSubscriber<?>>> subscribers = new HashMap<>();
	static {
		for (Message m : ServiceLoader.load(Message.class)) {
			messages.put(m.getKey(), m);
		}
		for (IMessageSubscriber<?> s : ServiceLoader.load(IMessageSubscriber.class)) {
			subscribe(s);
		}
	}

	public static Message decode(byte[] _data) {
		byte messageCheckSum = Message.calculateChecksum(_data);
		byte messageCheckSumReceived = _data[_data.length - 1];
		if (messageCheckSum != messageCheckSumReceived) {
			logger.debug("Invalid checksum for message: {}", NullUtils.toHex(_data));
			return null;
		}
		MessageType messageType = _data[2] == 0x00 ? MessageType.REQUEST : MessageType.RESPONSE;
		ControllerMessageType controllerMessageType = ControllerMessageType.fromByte((byte)(_data[3] & 0xFF));
		CommandClass commandClass = CommandClass.NO_OPERATION;
		byte command = 0;
		int offset = 5;
		if (NullUtils.isOneOf(controllerMessageType, ControllerMessageType.SendData, ControllerMessageType.ApplicationCommandHandler)) {
			if (messageType == MessageType.REQUEST)
				offset = 7;
			if (_data.length > offset + 1)
				commandClass = CommandClass.fromByte((byte)(_data[offset] & 0xFF));
			if (_data.length > offset + 2)
				command = (byte)(_data[offset+1] & 0xFF);
		}
		Message message = messages.get(Message.toKey(controllerMessageType.data, messageType.data, commandClass.data, command));
		if (message == null) {
			logger.debug("Could not find message class for message: {} {} {} {}", controllerMessageType.label, messageType.name(), commandClass.label, command);
			return null;
		}
		try {
			Message ret = message.getClass().getDeclaredConstructor().newInstance();
			ret.fromPayload(_data);
			return ret;
		} catch (Exception _e) {
			_e.printStackTrace();
			return null;
		}
	}

	public static void publish(Message _m) {
		for (IMessageSubscriber s : CollectionUtils.makeNotNull(subscribers.get(_m.getClass()))) {
			s.onMessage(_m);
		}
	}

	public static void subscribe(IMessageSubscriber<?> _subscriber) {
		CollectionUtils.addToMultiMap(_subscriber.getHandledMessageClass(), _subscriber, subscribers);
	}
}
