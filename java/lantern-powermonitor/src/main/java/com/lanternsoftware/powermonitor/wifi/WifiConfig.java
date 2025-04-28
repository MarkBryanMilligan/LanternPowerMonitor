package com.lanternsoftware.powermonitor.wifi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class WifiConfig {
	private static final Logger LOG = LoggerFactory.getLogger(WifiConfig.class);

	public static void setCredentials(String _ssid, String _password) {
		try {
			Runtime.getRuntime().exec(new String[]{"nmcli","d", "wifi", "connect", _ssid, "password", _password});
			Runtime.getRuntime().exec(new String[]{"history", "-c"});
		} catch (IOException _e) {
			LOG.error("Exception occurred while trying to reboot", _e);
		}
	}
}
