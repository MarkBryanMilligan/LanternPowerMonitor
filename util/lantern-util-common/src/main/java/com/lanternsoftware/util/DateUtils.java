package com.lanternsoftware.util;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public abstract class DateUtils {
    public static long toLong(Date _dt) {
        if (_dt == null)
            return Long.MIN_VALUE;
        return _dt.getTime();
    }

    public static Date toDate(long _epochOffset) {
        if (_epochOffset == Long.MIN_VALUE)
            return null;
        return new Date(_epochOffset);
    }

    public static Date millisecondsFromNow(long _milliseconds) {
        return new Date(new Date().getTime() + _milliseconds);
    }

    public static Date secondsFromNow(long _seconds) {
        return addSeconds(new Date(), _seconds);
    }

    public static Date minutesFromNow(int _minutes) {
        return addMinutes(new Date(), _minutes);
    }

    public static Date hoursFromNow(int _hours) {
        return addHours(new Date(), _hours);
    }

    public static Date daysFromNow(int _days) {
        return addDays(new Date(), _days);
    }

    public static Date addSeconds(Date _dt, long _seconds) {
        if (_dt == null)
            return null;
        return new Date(_dt.getTime() + _seconds * 1000L);
    }

    public static Date addMinutes(Date _dt, int _minutes) {
        if (_dt == null)
            return null;
        return new Date(_dt.getTime() + _minutes * 60000L);
    }

    public static Date addHours(Date _dt, int _hours) {
        if (_dt == null)
            return null;
        return new Date(_dt.getTime() + _hours * 3600000L);
    }

    public static Date addDays(Date _dt, int _days) {
        if (_dt == null)
            return null;
        return new Date(_dt.getTime() + _days * 86400000L);
    }

    public static Date addDays(Date _dt, int _days, TimeZone _tz) {
        if (_dt == null)
            return null;
        Calendar cal = toCalendar(_dt, _tz);
        cal.add(Calendar.DAY_OF_YEAR, _days);
        return cal.getTime();
    }

    public static Date addMonths(Date _dt, int _months) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(_dt);
        cal.add(Calendar.MONTH, _months);
        return cal.getTime();
    }

    public static Date addMonths(Date _dt, int _months, TimeZone _tz) {
        Calendar cal = toCalendar(_dt, _tz);
        if (cal == null)
            return null;
        cal.add(Calendar.MONTH, _months);
        return cal.getTime();
    }

    public static Date addMonthKeepDayOfWeek(Date _dt, int _months, TimeZone _tz) {
        Calendar cal = toCalendar(_dt, _tz);
        if (cal == null)
            return null;
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int dayOfWeekInMonth = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        cal.add(Calendar.MONTH, _months);
        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        cal.set(Calendar.DAY_OF_WEEK_IN_MONTH, dayOfWeekInMonth);
        return cal.getTime();
    }

    public static Date addYears(Date _dt, int _years) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(_dt);
        cal.add(Calendar.YEAR, _years);
        return cal.getTime();
    }

    public static Date addYears(Date _dt, int _years, TimeZone _tz) {
        Calendar cal = toCalendar(_dt, _tz);
        if (cal == null)
            return null;
        cal.add(Calendar.YEAR, _years);
        return cal.getTime();
    }

    public static long diffInMilliseconds(Date _dt1, Date _dt2) {
        return diffInMilliseconds(_dt1, _dt2, Long.MAX_VALUE);
    }

    public static long diffInMilliseconds(Date _dt1, Date _dt2, long _defaultIfNull) {
        if ((_dt1 == null) || (_dt2 == null))
            return _defaultIfNull;
        return Math.abs(_dt1.getTime() - _dt2.getTime());
    }

    public static long diffInSeconds(Date _dt1, Date _dt2) {
        return diffInSeconds(_dt1, _dt2, Long.MAX_VALUE);
    }

    public static long diffInSeconds(Date _dt1, Date _dt2, long _defaultIfNull) {
        if ((_dt1 == null) || (_dt2 == null))
            return _defaultIfNull;
        return Math.abs(_dt1.getTime() - _dt2.getTime()) / 1000;
    }

    public static long diffInMinutes(Date _dt1, Date _dt2) {
        return diffInMinutes(_dt1, _dt2, Long.MAX_VALUE);
    }

    public static long diffInMinutes(Date _dt1, Date _dt2, long _defaultIfNull) {
        if ((_dt1 == null) || (_dt2 == null))
            return _defaultIfNull;
        return Math.abs(_dt1.getTime() - _dt2.getTime()) / 60000;
    }

    public static long diffInHours(Date _dt1, Date _dt2) {
        return diffInHours(_dt1, _dt2, Long.MAX_VALUE);
    }

    public static long diffInHours(Date _dt1, Date _dt2, long _defaultIfNull) {
        if ((_dt1 == null) || (_dt2 == null))
            return _defaultIfNull;
        return Math.abs(_dt1.getTime() - _dt2.getTime()) / 3600000;
    }

    public static boolean isAfter(Date _dt1, Date _dt2) {
        return isAfter(_dt1, _dt2, false);
    }

    public static boolean isAfter(Date _dt1, Date _dt2, boolean _defaultIfNull) {
        if ((_dt1 == null) || (_dt2 == null))
            return _defaultIfNull;
        return _dt1.after(_dt2);
    }

    public static boolean isAfterOrEqualTo(Date _dt1, Date _dt2) {
        return isAfterOrEqualTo(_dt1, _dt2, false);
    }

    public static boolean isAfterOrEqualTo(Date _dt1, Date _dt2, boolean _defaultIfNull) {
        if ((_dt1 == null) || (_dt2 == null))
            return _defaultIfNull;
        return _dt1.getTime() >= _dt2.getTime();
    }

    public static boolean isBefore(Date _dt1, Date _dt2) {
        return isBefore(_dt1, _dt2, false);
    }

    public static boolean isBefore(Date _dt1, Date _dt2, boolean _defaultIfNull) {
        if ((_dt1 == null) || (_dt2 == null))
            return _defaultIfNull;
        return _dt1.before(_dt2);
    }

    public static boolean isBeforeOrEqualTo(Date _dt1, Date _dt2) {
        return isBeforeOrEqualTo(_dt1, _dt2, false);
    }

    public static boolean isBeforeOrEqualTo(Date _dt1, Date _dt2, boolean _defaultIfNull) {
        if ((_dt1 == null) || (_dt2 == null))
            return _defaultIfNull;
        return _dt1.getTime() <= _dt2.getTime();
    }

    public static String getAge(Date _dtDOB) {
        if (_dtDOB == null)
            return "";
        return getAge(_dtDOB, getMidnightBeforeNow());
    }

    public static String getAge(Date _dtDOB, Date _dtReference) {
        if (_dtDOB == null)
            return "";
        return getAge(_dtDOB.getTime(), _dtReference.getTime());
    }

    public static String getAge(long _dob, long _reference) {
        long lAge = _reference - _dob;
        if (lAge < 24 * 3600000) // less than a day old
            return String.format("%.2d:%.2d hours", lAge / 3600000, (lAge % 3600000) / 60000);
        if (lAge < 7 * 24 * 3600000) // less than a week old
            return String.format("%d days", lAge / (24 * 3600000));
        Date dtStart = new Date(_dob);
        Date dtEnd = new Date(_reference);
        int iMonths = getMonthsBetween(dtStart, dtEnd);
        if (iMonths == 0)
            return String.format("%d days", (dtEnd.getTime() - dtStart.getTime()) / (7 * 24 * 3600000));
        int iYears = getYearsBetween(dtStart, dtEnd);
        if (iYears < 2)
            return String.format("%d months", iMonths);
        return String.format("%d years", iYears);
    }

    public static int getMonthsBetween(Date _dtStart, Date _dtEnd) {
        Calendar calStart = getGMTCalendar(_dtStart.getTime());
        Calendar calEnd = getGMTCalendar(_dtEnd.getTime());
        int diff = calEnd.get(Calendar.YEAR) * 24 + calEnd.get(Calendar.MONTH) - calStart.get(Calendar.YEAR) * 24 + calStart.get(Calendar.MONTH);
        if (calStart.get(Calendar.DAY_OF_MONTH) > calEnd.get(Calendar.DAY_OF_MONTH))
            diff--;
        return diff;
    }

    public static int getYearsBetween(Date _dtStart, Date _dtEnd) {
        Calendar calStart = getGMTCalendar(_dtStart.getTime());
        Calendar calEnd = getGMTCalendar(_dtEnd.getTime());
        int diff = calEnd.get(Calendar.YEAR) - calStart.get(Calendar.YEAR);
        if (isLaterInYear(calStart, calEnd))
            diff--;
        return diff;
    }

    public static int getAgeInYears(Date _dtDOB) {
        if (_dtDOB == null)
            return 0;
        return getAgeInYears(_dtDOB.getTime());
    }

    public static int getAgeInYears(Date _dtDOB, Date _dtReference) {
        if (_dtDOB == null)
            return 0;
        return getAgeInYears(_dtDOB.getTime(), _dtReference);
    }

    public static int getAgeInYears(long _lDOB) {
        return getAgeInYears(_lDOB, getMidnightBeforeNow());
    }

    public static int getAgeInYears(long _lDOB, Date _dtReference) {
        if (_lDOB == 0 || _dtReference == null)
            return 0;
        Calendar calDOB = getGMTCalendar(_lDOB);
        Calendar calToday = getGMTCalendar(_dtReference.getTime());

        int age = calToday.get(Calendar.YEAR) - calDOB.get(Calendar.YEAR);
        if (!isLaterInYear(calToday, calDOB))
            age--;
        return age;
    }

    public static Calendar getGMTCalendar(long _lTime) {
        return toCalendar(_lTime, TimeZone.getTimeZone("GMT"));
    }

    private static boolean isLaterInYear(Calendar _cal1, Calendar _cal2) {
        if (_cal1.get(Calendar.MONTH) > _cal2.get(Calendar.MONTH))
            return true;
        return (_cal1.get(Calendar.MONTH) == _cal2.get(Calendar.MONTH)) && (_cal1.get(Calendar.DAY_OF_MONTH) >= _cal2.get(Calendar.DAY_OF_MONTH));
    }

    public static boolean isSameDay(Date _d1, Date _d2, TimeZone _tz) {
        return getMidnightBefore(_d1, _tz).equals(getMidnightBefore(_d2, _tz));
    }

    public static boolean isSameDayOfWeek(Date _d1, Date _d2, TimeZone _tz) {
        return getDayOfWeek(_d1, _tz) == getDayOfWeek(_d2, _tz);
    }

    public static boolean isSameTimeOfDay(Date _d1, Date _d2, TimeZone _tz) {
        Calendar cal1 = toCalendar(_d1, _tz);
        Calendar cal2 = toCalendar(_d2, _tz);
        if ((cal1 == null) || (cal2 == null))
            return false;
        if (cal1.get(Calendar.HOUR_OF_DAY) != cal2.get(Calendar.HOUR_OF_DAY))
            return false;
        if (cal1.get(Calendar.MINUTE) != cal2.get(Calendar.MINUTE))
            return false;
        if (cal1.get(Calendar.SECOND) != cal2.get(Calendar.SECOND))
            return false;
        return (cal1.get(Calendar.MILLISECOND) == cal2.get(Calendar.MILLISECOND));
    }

    public static Date getMidnightBeforeNow() {
        return getMidnightBeforeNow(TimeZone.getTimeZone("GMT"));
    }

    public static Date getMidnightBeforeNow(TimeZone _tz) {
        return hoursAfterMidnight(new Date(), 0, _tz);
    }

    public static Calendar getMidnightBeforeNowCal(TimeZone _tz) {
        return hoursAfterMidnightCal(new Date(), 0, _tz);
    }

    public static Date getMidnightBefore(Date _dt, TimeZone _tz) {
        return hoursAfterMidnight(_dt, 0, _tz);
    }

    public static Calendar getMidnightBeforeCal(Date _dt, TimeZone _tz) {
        return hoursAfterMidnightCal(_dt, 0, _tz);
    }

    public static Date getMidnightAfterNow() {
        return getMidnightAfterNow(TimeZone.getTimeZone("GMT"));
    }

    public static Date getMidnightAfterNow(TimeZone _tz) {
        return hoursAfterMidnight(new Date(), 24, _tz);
    }

    public static Calendar getMidnightAfterNowCal(TimeZone _tz) {
        return hoursAfterMidnightCal(new Date(), 24, _tz);
    }

    public static Date getMidnightAfter(Date _dt, TimeZone _tz) {
        return hoursAfterMidnight(_dt, 24, _tz);
    }

    public static Calendar getMidnightAfterCal(Date _dt, TimeZone _tz) {
        return hoursAfterMidnightCal(_dt, 24, _tz);
    }

    public static Date hoursAfterMidnight(Date _dt, int _hours, TimeZone _tz) {
        return hoursAfterMidnightCal(_dt, _hours, _tz).getTime();
    }

    public static Calendar hoursAfterMidnightCal(Date _dt, int _hours, TimeZone _tz) {
        Calendar cal = toCalendar(_dt, _tz);
        if (cal == null)
            return null;
        cal.set(Calendar.HOUR_OF_DAY, _hours);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public static boolean isBetween(Date _dt, Date _dtFrom, Date _dtTo) {
        if (_dt == null)
            return false;
        if ((_dtFrom != null) && _dtFrom.after(_dt))
            return false;
        return (_dtTo == null) || _dtTo.after(_dt);
    }

    public static Date setTimeOfDay(Date _date, Date _time, TimeZone _tz) {
        Calendar date = toCalendar(_date, _tz);
        Calendar time = toCalendar(_time, _tz);
        if ((date == null) || (time == null))
            return null;
        date.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
        date.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
        date.set(Calendar.SECOND, time.get(Calendar.SECOND));
        date.set(Calendar.MILLISECOND, time.get(Calendar.MILLISECOND));
        return date.getTime();
    }

    public static Calendar toCalendar(long _ts, TimeZone _tz) {
        return toCalendar(new Date(_ts), _tz);
    }

    public static Calendar toCalendar(Date _date, TimeZone _tz) {
        if (_date == null)
            return null;
        Calendar cal = new GregorianCalendar(_tz);
        cal.setTime(_date);
        return cal;
    }

    public static DateFormat dateFormat(String _format, TimeZone _tz) {
        SimpleDateFormat format = new SimpleDateFormat(_format);
        format.setTimeZone(_tz);
        return format;
    }

    public static String format(String _format, Date _dt) {
        return format(_format, TimeZone.getTimeZone("UTC"), _dt);
    }

    public static String format(String _format, TimeZone _tz, Date _dt) {
        if (_dt == null)
            return null;
        return dateFormat(_format, _tz).format(_dt);
    }

    public static Date parse(String _format, String _date) {
        return parse(_format, TimeZone.getTimeZone("UTC"), _date);
    }

    public static Date parse(String _format, TimeZone _tz, String _date) {
        if (NullUtils.isEmpty(_date))
            return null;
        try {
            return dateFormat(_format, _tz).parse(_date);
        }
        catch (Exception _e) {
            return null;
        }
    }

    public static Date date(int _month, int _day, int _year, TimeZone _tz) {
        return date(_month, _day, _year, 0, 0, 0, 0, _tz);
    }

    public static Date date(int _month, int _day, int _year, int _hour, int _minutes, int _seconds, int _ms, TimeZone _tz) {
        Calendar cal = GregorianCalendar.getInstance(_tz);
        cal.set(Calendar.YEAR, _year);
        cal.set(Calendar.MONTH, _month - 1);
        cal.set(Calendar.DAY_OF_MONTH, _day);
        cal.set(Calendar.HOUR_OF_DAY, _hour);
        cal.set(Calendar.MINUTE, _minutes);
        cal.set(Calendar.SECOND, _seconds);
        cal.set(Calendar.MILLISECOND, _ms);
        return cal.getTime();
    }

    public static int getDayOfWeek(Date _dt, TimeZone _tz) {
        Calendar cal = toCalendar(_dt, _tz);
        return cal == null ? 0 : cal.get(Calendar.DAY_OF_WEEK);
    }

    public static Date setDayOfWeek(Date _dt, TimeZone _tz, int _dayOfWeek) {
        Calendar cal = toCalendar(_dt, _tz);
        if (cal == null)
            return null;
        if ((_dayOfWeek >= Calendar.SUNDAY) && (_dayOfWeek <= Calendar.SATURDAY))
            cal.set(Calendar.DAY_OF_WEEK, _dayOfWeek);
        return cal.getTime();
    }

    public static Date getMidnightBeforeDayOfWeek(Date _dt, TimeZone _tz, int _dayOfWeek) {
        return getMidnightBefore(setDayOfWeek(_dt, _tz, _dayOfWeek), _tz);
    }

    public static Date getMidnightAfterDayOfWeek(Date _dt, TimeZone _tz, int _dayOfWeek) {
        return getMidnightAfter(setDayOfWeek(_dt, _tz, _dayOfWeek), _tz);
    }

    public static boolean isDstTransitionDay(Date _dt, TimeZone _tz) {
        Date midnight = getMidnightBefore(_dt, _tz);
        Calendar cal = toCalendar(midnight, _tz);
        if (cal == null)
            return false;
        cal.set(Calendar.HOUR_OF_DAY, 8);
        return (cal.getTimeInMillis() - midnight.getTime() != 28800000);
    }

    public static Date setDayOfWeek(Date _dt, TimeZone _tz, String _dayOfWeek) {
        Calendar cal = toCalendar(_dt, _tz);
        if (cal == null)
            return null;
        int dayOfWeekInt = 0;
        switch (_dayOfWeek) {
            case "Sunday":
                dayOfWeekInt = Calendar.SUNDAY;
                break;
            case "Monday":
                dayOfWeekInt = Calendar.MONDAY;
                break;
            case "Tuesday":
                dayOfWeekInt = Calendar.TUESDAY;
                break;
            case "Wednesday":
                dayOfWeekInt = Calendar.WEDNESDAY;
                break;
            case "Thursday":
                dayOfWeekInt = Calendar.THURSDAY;
                break;
            case "Friday":
                dayOfWeekInt = Calendar.FRIDAY;
                break;
            case "Saturday":
                dayOfWeekInt = Calendar.SATURDAY;
                break;
        }
        if (dayOfWeekInt > 0)
            cal.set(Calendar.DAY_OF_WEEK, dayOfWeekInt);

        return cal.getTime();
    }

    public static Date getStartOfMinute(TimeZone _tz) {
        return getStartOfMinute(new Date(), _tz);
    }

    public static Date getStartOfMinute(Date _dt, TimeZone _tz) {
        return getStartOfMinuteCal(_dt, _tz).getTime();
    }

    public static Calendar getStartOfMinuteCal(Date _dt, TimeZone _tz) {
        Calendar cal = toCalendar(_dt, _tz);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public static Date getEndOfMinute(TimeZone _tz) {
        return getEndOfMinute(new Date(), _tz);
    }

    public static Date getEndOfMinute(Date _dt, TimeZone _tz) {
        return getEndOfMinuteCal(_dt, _tz).getTime();
    }

    public static Calendar getEndOfMinuteCal(Date _dt, TimeZone _tz) {
        Calendar cal = getStartOfMinuteCal(_dt, _tz);
        cal.add(Calendar.MINUTE, 1);
        return cal;
    }

    public static Date getStartOfHour(TimeZone _tz) {
        return getStartOfHour(new Date(), _tz);
    }

    public static Date getStartOfHour(Date _dt, TimeZone _tz) {
        return getStartOfHourCal(_dt, _tz).getTime();
    }

    public static Calendar getStartOfHourCal(Date _dt, TimeZone _tz) {
        Calendar cal = toCalendar(_dt, _tz);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public static Date getEndOfHour(TimeZone _tz) {
        return getEndOfHour(new Date(), _tz);
    }

    public static Date getEndOfHour(Date _dt, TimeZone _tz) {
        return getEndOfHourCal(_dt, _tz).getTime();
    }

    public static Calendar getEndOfHourCal(Date _dt, TimeZone _tz) {
        Calendar cal = getStartOfHourCal(_dt, _tz);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        return cal;
    }

    public static Date getStartOfWeek(TimeZone _tz) {
        return getStartOfWeek(new Date(), _tz);
    }

    public static Date getStartOfWeek(Date _dt, TimeZone _tz) {
        return getStartOfWeekCal(_dt, _tz).getTime();
    }

    public static Calendar getStartOfWeekCal(Date _dt, TimeZone _tz) {
        Calendar cal = toCalendar(_dt, _tz);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return cal;
    }

    public static Date getEndOfWeek(TimeZone _tz) {
        return getEndOfWeek(new Date(), _tz);
    }

    public static Date getEndOfWeek(Date _dt, TimeZone _tz) {
        return getEndOfWeekCal(_dt, _tz).getTime();
    }

    public static Calendar getEndOfWeekCal(Date _dt, TimeZone _tz) {
        Calendar cal = getStartOfWeekCal(_dt, _tz);
        cal.add(Calendar.DAY_OF_YEAR, 7);
        return cal;
    }

    public static Date getStartOfMonth(TimeZone _tz) {
        return getStartOfMonth(new Date(), _tz);
    }

    public static Date getStartOfMonth(Date _dt, TimeZone _tz) {
        return getStartOfMonthCal(_dt, _tz).getTime();
    }

    public static Calendar getStartOfMonthCal(Date _dt, TimeZone _tz) {
        Calendar cal = toCalendar(_dt, _tz);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal;
    }

    public static Date getEndOfMonth(TimeZone _tz) {
        return getEndOfMonth(new Date(), _tz);
    }

    public static Date getEndOfMonth(Date _dt, TimeZone _tz) {
        return getEndOfMonthCal(_dt, _tz).getTime();
    }

    public static Calendar getEndOfMonthCal(Date _dt, TimeZone _tz) {
        Calendar cal = getStartOfMonthCal(_dt, _tz);
        cal.add(Calendar.MONTH, 1);
        return cal;
    }

    public static Date getStartOfYear(TimeZone _tz) {
        return getStartOfYear(new Date(), _tz);
    }

    public static Date getStartOfYear(Date _dt, TimeZone _tz) {
        return getStartOfYearCal(_dt, _tz).getTime();
    }

    public static Calendar getStartOfYearCal(Date _dt, TimeZone _tz) {
        Calendar cal = toCalendar(_dt, _tz);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        return cal;
    }

    public static Date getEndOfYear(TimeZone _tz) {
        return getEndOfYear(new Date(), _tz);
    }

    public static Date getEndOfYear(Date _dt, TimeZone _tz) {
        return getEndOfYearCal(_dt, _tz).getTime();
    }

    public static Calendar getEndOfYearCal(Date _dt, TimeZone _tz) {
        Calendar cal = getStartOfYearCal(_dt, _tz);
        cal.add(Calendar.YEAR, 1);
        return cal;
    }

    public static String getTimeZoneId(TimeZone _tz) {
        return getTimeZoneId(_tz, "America/Chicago");
    }

    public static String getTimeZoneId(TimeZone _tz, String _default) {
        return (_tz == null) ? _default : _tz.getID();
    }

    public static TimeZone defaultTimeZone(TimeZone _tz) {
        return defaultTimeZone(_tz, "America/Chicago");
    }

    public static TimeZone defaultTimeZone(TimeZone _tz, String _default) {
        return (_tz == null) ? TimeZone.getTimeZone(_default) : _tz;
    }

    public static TimeZone fromTimeZoneId(String _id) {
        return fromTimeZoneId(_id, "America/Chicago");
    }

    public static TimeZone fromTimeZoneId(String _id, String _defaultId) {
        return TimeZone.getTimeZone(NullUtils.isEmpty(_id) ? _defaultId : _id);
    }
}
