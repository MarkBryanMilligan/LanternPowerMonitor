package com.lanternsoftware.powermonitor.wifi;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class WifiConfig {
	private static final Logger LOG = LoggerFactory.getLogger(WifiConfig.class);

	private static final String WIFI_CONFIG_PATH = "/etc/wpa_supplicant/wpa_supplicant.conf";
	private static final String CONF_FORMAT = "ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev\nupdate_config=1\ncountry=US\n";

	public static void setCredentials(String _ssid, String _password) {
		InputStream is = null;
		try {
			is = Runtime.getRuntime().exec(new String[]{"wpa_passphrase", _ssid, _password}).getInputStream();
			String newConf = CollectionUtils.delimit(CollectionUtils.filter(CollectionUtils.asArrayList(NullUtils.cleanSplit(IOUtils.toString(is, StandardCharsets.UTF_8), "\\r?\\n")), _s->!_s.trim().startsWith("#")), "\n");
			if (newConf == null)
				return;
			ResourceLoader.writeFile(WIFI_CONFIG_PATH, CONF_FORMAT+newConf);
			Runtime.getRuntime().exec(new String[]{"wpa_cli","-i","wlan0","reconfigure"});
		}
		catch (Exception _e) {
			LOG.error("Failed to write wifi credentials", _e);
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}
}
