package com.lanternsoftware.powermonitor.servlet.console;

public class MonthDisplay {
	public final String name;
	public final long date;
	public final int progress;

	public MonthDisplay(String _name, long _date, int _progress) {
		name = _name;
		date = _date;
		progress = _progress;
	}

	public String getName() {
		return name;
	}

	public String getFileName() {
		return name.replace(" ", "-");
	}

	public String getDate() {
		return String.valueOf(date);
	}

	public int getProgress() {
		return progress;
	}
}
