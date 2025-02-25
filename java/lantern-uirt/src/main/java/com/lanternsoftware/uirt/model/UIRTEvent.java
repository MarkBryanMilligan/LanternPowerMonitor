package com.lanternsoftware.uirt.model;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class UIRTEvent extends Structure {
	public static class ByReference extends UIRTEvent implements Structure.ByReference {}

	public int code;
	public String data;

	public UIRTEvent() {
	}

	public UIRTEvent(Pointer p) {
		super(p);
	}

	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("code", "data");
	}
}
