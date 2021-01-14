package com.lanternsoftware.datamodel.zwave;

import java.util.Date;

public class SwitchTransition {
	private final Switch sw;
	private final Date transitionTime;
	private final int level;

	SwitchTransition(Switch _sw, Date _transitionTime, int _level) {
		sw = _sw;
		transitionTime = _transitionTime;
		level = _level;
	}

	public Switch getSwitch() {
		return sw;
	}

	public Date getTransitionTime() {
		return transitionTime;
	}

	public int getLevel() {
		return level;
	}
}
