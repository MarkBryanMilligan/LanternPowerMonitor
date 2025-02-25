package com.lanternsoftware.datamodel.zwave.device.zooz.settings;

import com.lanternsoftware.datamodel.zwave.device.ZWaveSetting;
import com.lanternsoftware.datamodel.zwave.device.ZWaveSettingOption;
import com.lanternsoftware.util.CollectionUtils;

public class ZoozPaddleOrientation extends ZWaveSetting {
	public ZoozPaddleOrientation() {
		super(1, CollectionUtils.asArrayList(new ZoozPaddleOrientationTopOn(), new ZoozPaddleOrientationTopOff(), new ZoozPaddleOrientationToggle()));
	}

	public static final class ZoozPaddleOrientationTopOn extends ZWaveSettingOption {
		public ZoozPaddleOrientationTopOn() {
			super("Upper paddle turns the light on, lower paddle turns it off (default)", new byte[]{0});
		}
	}

	public static final class ZoozPaddleOrientationTopOff extends ZWaveSettingOption {
		public ZoozPaddleOrientationTopOff() {
			super("Upper paddle turns the light off, lower paddle turns it on", new byte[]{1});
		}
	}

	public static final class ZoozPaddleOrientationToggle extends ZWaveSettingOption {
		public ZoozPaddleOrientationToggle() {
			super("Any paddle turns light on/off", new byte[]{2});
		}
	}
}
