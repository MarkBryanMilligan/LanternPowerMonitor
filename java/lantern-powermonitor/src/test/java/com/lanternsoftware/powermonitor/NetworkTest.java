package com.lanternsoftware.powermonitor;

import com.lanternsoftware.powermonitor.util.NetworkMonitor;
import com.lanternsoftware.powermonitor.datamodel.NetworkStatus;

public class NetworkTest {
	public static void main(String[] args) {
		NetworkStatus status = NetworkMonitor.getNetworkStatus();
	}
}
