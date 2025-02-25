package com.lanternsoftware.powermonitor.datamodel.archive;

import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.List;

@DBSerializable
public class DailyEnergyArchive {
	private List<BreakerEnergyArchive> breakers;

	public List<BreakerEnergyArchive> getBreakers() {
		return breakers;
	}

	public void setBreakers(List<BreakerEnergyArchive> _breakers) {
		breakers = _breakers;
	}
}
