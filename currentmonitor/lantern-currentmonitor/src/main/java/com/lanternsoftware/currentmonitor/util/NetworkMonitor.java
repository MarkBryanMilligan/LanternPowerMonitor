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
		String[] commands = {"ifconfig", "-a"};
		InputStream is = null;
		try {
			is = Runtime.getRuntime().exec(commands).getInputStream();
			return getNetworkStatus(IOUtils.toString(is));
		}
		catch (Exception _e) {
			LOG.error("Failed to check network state", _e);
			return new NetworkStatus();
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}

	public static NetworkStatus getNetworkStatus(String _ifconfig) {
		NetworkStatus status = new NetworkStatus();
		status.setEthernetIPs(parseEthernetIp(_ifconfig));
		status.setWifiIPs(parseWifiIp(_ifconfig));
		return status;
	}

	public static List<String> parseWifiIp(String _ifconfig) {
		return getIPs(_ifconfig, wifiPattern);
	}

	public static List<String> parseEthernetIp(String _ifconfig) {
		return getIPs(_ifconfig, ethernetPattern);
	}

	private static List<String> getIPs(String _ifConfig, Pattern _pattern) {
		List<String> ips = new ArrayList<>();
		Matcher m = _pattern.matcher(_ifConfig);
		while (m.find()) {
			int start = m.start(0);
			int ipStart = _ifConfig.indexOf("inet ", start) + 5;
			int deviceEnd = _ifConfig.indexOf("\n\n", start);
			if (ipStart > 4) {
				int ipEnd = _ifConfig.indexOf(" ", ipStart);
				if ((ipEnd > -1) && ((deviceEnd < 0) || (ipEnd <= deviceEnd)))
					ips.add(_ifConfig.substring(ipStart, ipEnd));
			}
		}
		return ips;
	}
}
