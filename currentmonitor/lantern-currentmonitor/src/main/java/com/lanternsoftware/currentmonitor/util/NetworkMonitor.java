package com.lanternsoftware.currentmonitor.util;

import com.lanternsoftware.datamodel.currentmonitor.NetworkStatus;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class NetworkMonitor {
	private static final Logger LOG = LoggerFactory.getLogger(NetworkMonitor.class);

	private static final Pattern ethernetPattern = Pattern.compile(".*(eth[0-9]):(.*)");
	private static final Pattern wifiPattern = Pattern.compile(".*(wlan[0-9]):(.*)");

	public static NetworkStatus getNetworkStatus() {
		NetworkStatus status = new NetworkStatus();
		String[] commands = {"ifconfig", "-a"};
		InputStream is = null;
		try {
			is = Runtime.getRuntime().exec(commands).getInputStream();
			String ifconfig = IOUtils.toString(is);
			status.setEthernetIPs(getIPs(ifconfig, ethernetPattern));
			status.setWifiIPs(getIPs(ifconfig, wifiPattern));
		}
		catch (Exception _e) {
			LOG.error("Failed to check network state", _e);
		}
		finally {
			IOUtils.closeQuietly(is);
		}
		return status;
	}

	private static List<String> getIPs(String _ifConfig, Pattern _pattern) {
		List<String> ips = new ArrayList<>();
		Matcher m = _pattern.matcher(_ifConfig);
		while (m.find()) {
			int start = m.start(0);
			int ipStart = _ifConfig.indexOf("inet ", start) + 5;
			if (ipStart > 4) {
				int ipEnd = _ifConfig.indexOf(" ", ipStart);
				if (ipEnd > -1)
					ips.add(_ifConfig.substring(ipStart, ipEnd));
			}
		}
		return ips;
	}
}
