package com.lanternsoftware.datamodel.currentmonitor;


import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Date;

@DBSerializable
public class EnergyBlock {
	private Date start;
	private Date end;
	private double joules;

	public EnergyBlock() {
	}

	public EnergyBlock(Date _start, Date _end, double _joules) {
		start = _start;
		end = _end;
		joules = _joules;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date _start) {
		start = _start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date _end) {
		end = _end;
	}

	public double getJoules() {
		return joules;
	}

	public void addJoules(double _joules) {
		joules += _joules;
	}

	public void setJoules(double _joules) {
		joules = _joules;
	}

	public double wattHours() {
		return joules / 3600;
	}

	public double getAveragePower() {
		if ((end == null) || (start == null))
			return 0;
		return 1000*joules/(end.getTime()-start.getTime());
	}
}
