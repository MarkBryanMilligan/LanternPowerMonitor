package com.lanternsoftware.uirt.model;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class UIRTConfig extends Structure {
	public static class ByReference extends UIRTConfig implements Structure.ByReference {}

	public boolean ledRX;
	public boolean ledTX;
	public boolean legacyRX;

	public UIRTConfig() {
	}

	public UIRTConfig(Pointer _value) {
		super(_value);
	}

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("ledRX", "ledTX", "legacyRX");
	}

	public boolean isLedRX() {
		return ledRX;
	}

	public void setLedRX(boolean _ledRX) {
		ledRX = _ledRX;
	}

	public boolean isLedTX() {
		return ledTX;
	}

	public void setLedTX(boolean _ledTX) {
		ledTX = _ledTX;
	}

	public boolean isLegacyRX() {
		return legacyRX;
	}

	public void setLegacyRX(boolean _legacyRX) {
		legacyRX = _legacyRX;
	}
}
