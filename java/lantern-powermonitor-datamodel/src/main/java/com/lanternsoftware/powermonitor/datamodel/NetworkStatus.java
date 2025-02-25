package com.lanternsoftware.powermonitor.datamodel;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.EnumSet;
import java.util.List;

@DBSerializable
public class NetworkStatus {
	private List<String> wifiIPs;
	private List<String> ethernetIPs;
	private boolean pingSuccessful = true;

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

	public boolean isPingSuccessful() {
		return pingSuccessful;
	}

	public void setPingSuccessful(boolean _pingSuccessful) {
		pingSuccessful = _pingSuccessful;
	}

	public boolean isNetworkConnected() {
		return isWifiConnected() || isEthernetConnected();
	}

	public boolean isWifiConnected() {
		return CollectionUtils.isNotEmpty(wifiIPs);
	}

	public boolean isEthernetConnected() {
		return CollectionUtils.isNotEmpty(ethernetIPs);
	}

	public byte toMask() {
		EnumSet<NetworkAdapter> adapters = EnumSet.noneOf(NetworkAdapter.class);
		if (isWifiConnected() && isPingSuccessful())
			adapters.add(NetworkAdapter.WIFI);
		if (isEthernetConnected() && isPingSuccessful())
			adapters.add(NetworkAdapter.ETHERNET);
		return NetworkAdapter.toMask(adapters);
	}
}
