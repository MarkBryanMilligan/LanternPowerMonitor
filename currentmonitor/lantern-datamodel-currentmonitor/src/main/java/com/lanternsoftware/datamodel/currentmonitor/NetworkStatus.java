package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.CollectionUtils;

import java.util.EnumSet;
import java.util.List;

public class NetworkStatus {
	private List<String> wifiIPs;
	private List<String> ethernetIPs;

	public List<String> getWifiIPs() {
		return wifiIPs;
	}

	public void setWifiIPs(List<String> _wifiIPs) {
		wifiIPs = _wifiIPs;
	}

	public List<String> getEthernetIPs() {
		return ethernetIPs;
	}

	public void setEthernetIPs(List<String> _ethernetIPs) {
		ethernetIPs = _ethernetIPs;
	}

	public boolean isWifiConnected() {
		return CollectionUtils.isNotEmpty(wifiIPs);
	}

	public boolean isEthernetConnected() {
		return CollectionUtils.isNotEmpty(ethernetIPs);
	}

	public byte toMask() {
		EnumSet<NetworkAdapter> adapters = EnumSet.noneOf(NetworkAdapter.class);
		if (isWifiConnected())
			adapters.add(NetworkAdapter.WIFI);
		if (isEthernetConnected())
			adapters.add(NetworkAdapter.ETHERNET);
		return NetworkAdapter.toMask(adapters);
	}
}
