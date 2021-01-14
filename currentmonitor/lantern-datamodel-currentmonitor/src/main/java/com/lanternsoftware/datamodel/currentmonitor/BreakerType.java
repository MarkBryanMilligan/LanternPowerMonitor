package com.lanternsoftware.datamodel.currentmonitor;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;

import java.util.List;

public enum BreakerType {
	EMPTY("Empty"),
	SINGLE_POLE("Single Pole"),
	SINGLE_POLE_TANDEM("Single Pole Tandem (Two Breakers in One)"),
	DOUBLE_POLE_TOP("Double Pole (240V)"),
	DOUBLE_POLE_BOTTOM("Double Pole (240V)");

	private final String display;

	BreakerType(String _display) {
		display = _display;
	}

	public String getDisplay() {
		return display;
	}

	public static List<BreakerType> selectable() {
		return CollectionUtils.asArrayList(EMPTY, SINGLE_POLE, SINGLE_POLE_TANDEM, DOUBLE_POLE_TOP);
	}

	public static BreakerType fromDisplay(String _display) {
		for (BreakerType type : values()) {
			if (NullUtils.isEqual(_display, type.getDisplay()))
				return type;
		}
		return null;
	}
}
