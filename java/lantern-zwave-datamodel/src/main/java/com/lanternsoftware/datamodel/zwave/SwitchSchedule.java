package com.lanternsoftware.datamodel.zwave;


import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.TimeZone;

@DBSerializable
public class SwitchSchedule {
	private int dayOfWeek;
	private int timeOfDay;
	private int timeOfDayEnd;
	private int onDuration;
	private int offDuration;
	private int level;

	public SwitchSchedule() {
	}

	public SwitchSchedule(int _onDuration, int _offDuration) {
		onDuration = _onDuration;
		offDuration = _offDuration;
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

	public int getTimeOfDayEnd() {
		return timeOfDayEnd;
	}

	public void setTimeOfDayEnd(int _timeOfDayEnd) {
		timeOfDayEnd = _timeOfDayEnd;
	}
	public void setTimeOfDayEnd(int _hour, int _minute) {
		timeOfDayEnd = (_hour * 3600) + (_minute * 60);
	}
	public void setTimeOfDayEnd(int _hour, int _minute, int _second) {
		timeOfDayEnd = (_hour * 3600) + (_minute * 60) + _second;
	}

	public int getOnDuration() {
		return onDuration;
	}

	public void setOnDuration(int _onDuration) {
		onDuration = _onDuration;
	}

	public int getOffDuration() {
		return offDuration;
	}

	public void setOffDuration(int _offDuration) {
		offDuration = _offDuration;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int _level) {
		level = _level;
	}

	public int hour() {
		return hour(timeOfDay);
	}

	public int minute() {
		return minute(timeOfDay);
	}

	public int second() {
		return second(timeOfDay);
	}

	public int hour(int _timeOfDay) {
		return _timeOfDay/3600;
	}

	public int minute(int _timeOfDay) {
		return (_timeOfDay/60)%60;
	}

	public int second(int _timeOfDay) {
		return _timeOfDay%60;
	}

	public Date startToday(TimeZone _tz) {
		return today(timeOfDay, _tz);
	}

	public Date endToday(TimeZone _tz) {
		return today(timeOfDayEnd, _tz);
	}

	public Date today(int _timeOfDay, TimeZone _tz) {
		Date now = new Date();
		Calendar cal = DateUtils.toCalendar(now, _tz);
		if (dayOfWeek > 0)
			cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		cal.set(Calendar.HOUR_OF_DAY, hour(_timeOfDay));
		cal.set(Calendar.MINUTE, minute(_timeOfDay));
		cal.set(Calendar.SECOND, second(_timeOfDay));
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public SwitchTransition getNextTransition(Switch _switch, TimeZone _tz) {
		Date startToday = startToday(_tz);
		Date now = new Date();
		if (onDuration > 0) {
			if ((timeOfDay > 0) && now.before(startToday))
				now = startToday;
			if (timeOfDayEnd > 0) {
				if (now.after(endToday(_tz)))
					now = DateUtils.addDays(startToday, (dayOfWeek > 0)?7:1, _tz);
			}
			long progress = now.getTime()%((onDuration+offDuration)*1000L);
			if (progress < onDuration*1000L)
				return new SwitchTransition(_switch, new Date(now.getTime() + (onDuration*1000L)-progress), 0);
			return new SwitchTransition(_switch, new Date(now.getTime()+((onDuration+offDuration)*1000L)-progress), level == 0 ? 255 : level);
		}
		return new SwitchTransition(_switch, startToday.after(now)?startToday:DateUtils.addDays(startToday, (dayOfWeek > 0)?7:1, _tz), level);
	}

	@Override
	public boolean equals(Object _o) {
		if (this == _o) return true;
		if (_o == null || getClass() != _o.getClass()) return false;
		SwitchSchedule that = (SwitchSchedule) _o;
		return dayOfWeek == that.dayOfWeek && timeOfDay == that.timeOfDay && timeOfDayEnd == that.timeOfDayEnd && onDuration == that.onDuration && offDuration == that.offDuration && level == that.level;
	}

	@Override
	public int hashCode() {
		return Objects.hash(dayOfWeek, timeOfDay, timeOfDayEnd, onDuration, offDuration, level);
	}

	public SwitchSchedule duplicate() {
		SwitchSchedule s = new SwitchSchedule();
		s.setDayOfWeek(getDayOfWeek());
		s.setTimeOfDay(getTimeOfDay());
		s.setTimeOfDayEnd(getTimeOfDayEnd());
		s.setOnDuration(getOnDuration());
		s.setOffDuration(getOffDuration());
		s.setLevel(getLevel());
		return s;
	}
}
