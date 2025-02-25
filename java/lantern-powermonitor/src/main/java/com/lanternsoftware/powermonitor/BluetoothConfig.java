package com.lanternsoftware.powermonitor;

import com.lanternsoftware.powermonitor.bluetooth.BleApplication;
import com.lanternsoftware.powermonitor.bluetooth.BleCharacteristic;
import com.lanternsoftware.powermonitor.bluetooth.BleCharacteristicListener;
import com.lanternsoftware.powermonitor.bluetooth.BleHelper;
import com.lanternsoftware.powermonitor.bluetooth.BleService;
import com.lanternsoftware.powermonitor.datamodel.HubConfigService;
import com.lanternsoftware.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BluetoothConfig {
	private static final Logger LOG = LoggerFactory.getLogger(BluetoothConfig.class);
	private final BleApplication app;

	public BluetoothConfig(String _hubName, BleCharacteristicListener _listener) {
		BleApplication a = null;
		try {
			BleHelper.getAdapter().setPowered(true);
			BleHelper.requestBusName("com.lanternsoftware");
			BleHelper.setBasePath("/com/lanternsoftware");
			List<BleCharacteristic> chars = CollectionUtils.transform(HubConfigService.getCharacteristics(), _c -> new BleCharacteristic("HubConfig", _c.getUUID(), _c.name(), _c.getFlags()));
			chars.forEach(_c -> _c.setListener(_listener));
			a = new BleApplication("Lantern", _hubName, new BleService("HubConfig", HubConfigService.getServiceUUID(), chars));
		}
		catch (Throwable _t) {
			LOG.error("Failed to initialize BLE service", _t);
		}
		app = a;
	}

	public void start() {
		if (app != null)
			app.start();
	}

	public void stop() {
		if (app != null)
			app.stop();
	}
}
