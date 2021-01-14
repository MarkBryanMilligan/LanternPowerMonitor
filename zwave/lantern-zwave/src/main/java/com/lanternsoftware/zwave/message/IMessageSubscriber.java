package com.lanternsoftware.zwave.message;

public interface IMessageSubscriber<T extends Message> {
	Class<T> getHandledMessageClass();
	void onMessage(T _message);
}
