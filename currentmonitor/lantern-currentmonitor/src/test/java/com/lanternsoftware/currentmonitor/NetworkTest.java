package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.currentmonitor.util.NetworkMonitor;
import com.lanternsoftware.datamodel.currentmonitor.NetworkStatus;

public class NetworkTest {
	public static void main(String[] args) {
		NetworkStatus status = NetworkMonitor.getNetworkStatus();
	}
}
