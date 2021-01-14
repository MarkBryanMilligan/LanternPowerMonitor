package com.lanternsoftware.datamodel.zwave;


import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

@DBSerializable
public class SwitchSchedule {
	private int dayOfWeek;
	private int timeOfDay;
	private int minutesPerHour;
	private int level;

	public SwitchSchedule() {
	}

	public SwitchSchedule(int _minutesPerHour) {
		minutesPerHour = _minutesPerHour;
	}

	public SwitchSchedule(int _dayOfWeek, int _timeOfDay, int _level) {
		dayOfWeek = _dayOfWeek;
		timeOfDay = _timeOfDay;
		level = _level;
	}

	public SwitchSchedule(int _dayOfWeek, int _hour, int _minute, int _level) {
		dayOfWeek = _dayOfWeek;
		setTimeOfDay(_hour, _minute);
		level = _level;
	}

	public SwitchSchedule(int _dayOfWeek, int _hour, int _minute, int _second, int _level) {
		dayOfWeek = _dayOfWeek;
		setTimeOfDay(_hour, _minute, _second);
		level = _level;
	}

	public int getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(int _dayOfWeek) {
		dayOfWeek = _dayOfWeek;
	}

	public int getTimeOfDay() {
		return timeOfDay;
	}

	public void setTimeOfDay(int _timeOfDayInSeconds) {
		timeOfDay = _timeOfDayInSeconds;
	}

	public void setTimeOfDay(int _hour, int _minute) {
		timeOfDay = (_hour * 3600) + (_minute * 60);
	}
	public void setTimeOfDay(int _hour, int _minute, int _second) {
		timeOfDay = (_hour * 3600) + (_minute * 60) + _second;
	}

	public int getMinutesPerHour() {
		return minutesPerHour;
	}

	public void setMinutesPerHour(int _minutesPerHour) {
		minutesPerHour = _minutesPerHour;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int _level) {
		level = _level;
	}

	public int hour() {
		return timeOfDay/3600;
	}

	public int minute() {
		return (timeOfDay/60)%60;
	}

	public int second() {
		return timeOfDay%60;
	}

	private boolean isOn() {
		return GregorianCalendar.getInstance().get(Calendar.MINUTE) < minutesPerHour;
	}

	public SwitchTransition getNextTransition(Switch _switch, TimeZone _tz) {
		if (minutesPerHour > 0) {
			Date dt = DateUtils.getStartOfHour(_tz);
			Date transition = DateUtils.addMinutes(dt, minutesPerHour);
			if (new Date().before(transition))
				return new SwitchTransition(_switch, transition, 0);
			return new SwitchTransition(_switch, DateUtils.getEndOfHour(_tz), level == 0?255:level);
		}
		Date now = new Date();
		Calendar cal = DateUtils.toCalendar(now, _tz);
		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		cal.set(Calendar.HOUR_OF_DAY, hour());
		cal.set(Calendar.MINUTE, minute());
		cal.set(Calendar.SECOND, second());
		cal.set(Calendar.MILLISECOND, 0);
		if (cal.getTimeInMillis() <= now.getTime())
			cal.add(Calendar.DAY_OF_MONTH, 7);
		return new SwitchTransition(_switch, cal.getTime(), level);
	}
}
