package com.lanternsoftware.util;

import java.util.Date;

public class DateRange {
	private Date start;
	private Date end;

	public DateRange() {
	}

	public DateRange(Date _start, Date _end) {
		start = _start;
		end = _end;
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
}
