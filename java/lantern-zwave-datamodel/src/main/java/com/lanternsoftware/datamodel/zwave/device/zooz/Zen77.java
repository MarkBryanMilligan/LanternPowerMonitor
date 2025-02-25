package com.lanternsoftware.datamodel.zwave.device.zooz;

import com.lanternsoftware.datamodel.zwave.device.ZWaveDevice;
import com.lanternsoftware.datamodel.zwave.device.zooz.settings.ZoozPaddleOrientation;
import com.lanternsoftware.util.CollectionUtils;

public class Zen77 extends ZWaveDevice {
	public Zen77() {
		super(CollectionUtils.asArrayList(new ZoozPaddleOrientation()));
	}
}
