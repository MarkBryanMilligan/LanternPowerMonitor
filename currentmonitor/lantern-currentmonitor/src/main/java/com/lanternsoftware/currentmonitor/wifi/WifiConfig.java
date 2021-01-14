package com.lanternsoftware.currentmonitor.wifi;

import com.lanternsoftware.util.ResourceLoader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public abstract class WifiConfig {
	private static final Logger LOG = LoggerFactory.getLogger(WifiConfig.class);

	private static final String WIFI_CONFIG_PATH = "/etc/wpa_supplicant/wpa_supplicant.conf";
	private static final String CONF_FORMAT = "ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev\nupdate_config=1\ncountry=US\nnetwork={\n\tssid=\"%s\"\n\t%s\n}\n";

	public static void setCredentials(String _ssid, String _password) {
		String[] commands = {"wpa_passphrase", _ssid, _password};
		InputStream is = null;
		try {
			is = Runtime.getRuntime().exec(commands).getInputStream();
			String newConf = IOUtils.toString(is);
			if (newConf == null)
				return;
			int idx = newConf.indexOf("psk=");
			if (idx > 0) {
				if (newConf.charAt(idx-1) == '#')
					idx = newConf.indexOf("psk=", idx+1);
				if (idx > 0) {
					int endIdx = newConf.indexOf("\n", idx);
					if (endIdx > 0) {
						String finalConf = String.format(CONF_FORMAT, _ssid, newConf.substring(idx, endIdx));
						ResourceLoader.writeFile(WIFI_CONFIG_PATH, finalConf);
					}
				}
			}
		}
		catch (Exception _e) {
			LOG.error("Failed to write wifi credentials", _e);
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}
}
