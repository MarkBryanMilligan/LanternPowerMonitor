package com.lanternsoftware.powermonitor.datamodel;


import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Date;

@DBSerializable
public class EnergyBlock {
	private Date start;
	private Date end;
	private double joules;
	private double charge;

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

	public double getCharge() {
		return charge;
	}

	public void setCharge(double _charge) {
		charge = _charge;
	}

	public void addCharge(double _charge) {
		charge += _charge;
	}

	public double wattHours() {
		return joules / 3600;
	}
}
