package com.lanternsoftware.powermonitor.datamodel;

public enum BreakerPolarity {
	NORMAL("Normal (CT Absolute Value)"),
	SOLAR("Solar (CT Negative Absolute Value)"),
	BI_DIRECTIONAL("Bi-Directional"),
	BI_DIRECTIONAL_INVERTED("Bi-Directional Inverted");

	private final String display;

	BreakerPolarity(String _display) {
		display = _display;
	}

	public String getDisplay() {
		return display;
	}
}
