package com.lanternsoftware.util;

import org.slf4j.helpers.MessageFormatter;

public abstract class MessageUtil {
	public static String msg(String _format, Object... _params) {
		return MessageFormatter.arrayFormat(_format, _params).getMessage();
	}
}
