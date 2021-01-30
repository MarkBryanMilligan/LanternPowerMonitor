package com.lanternsoftware.currentmonitor;

import com.lanternsoftware.currentmonitor.bluetooth.BleApplication;
import com.lanternsoftware.currentmonitor.bluetooth.BleCharacteristic;
import com.lanternsoftware.currentmonitor.bluetooth.BleCharacteristicListener;
import com.lanternsoftware.currentmonitor.bluetooth.BleHelper;
import com.lanternsoftware.currentmonitor.bluetooth.BleService;
import com.lanternsoftware.datamodel.currentmonitor.HubConfigService;
import com.lanternsoftware.util.CollectionUtils;

import java.util.List;

public class BluetoothConfig {
	private final BleApplication app;

	public BluetoothConfig(String _hubName, BleCharacteristicListener _listener) {
		BleHelper.getAdapter().setPowered(true);
		BleHelper.requestBusName("com.lanternsoftware");
		BleHelper.setBasePath("/com/lanternsoftware");
		HubConfigService service = new HubConfigService();
		List<BleCharacteristic> chars = CollectionUtils.transform(service.getCharacteristics(), _c->new BleCharacteristic("HubConfig", _c.getUUID(), _c.name(), _c.getFlags()));
		chars.forEach(_c->_c.setListener(_listener));
		app = new BleApplication("Lantern", _hubName, new BleService("HubConfig", service.getServiceUUID(), chars));
	}

	public void start() {
		app.start();
	}

	public void stop() {
		app.stop();
	}
}
