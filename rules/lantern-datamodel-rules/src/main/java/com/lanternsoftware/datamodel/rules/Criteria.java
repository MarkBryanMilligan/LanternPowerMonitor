package com.lanternsoftware.datamodel.rules;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

@DBSerializable
public class Criteria {
	private EventType type;
	private String sourceId;
	private Operator operator;
	private double value;
	private boolean or;
	private List<Criteria> criteria;

	public EventType getType() {
		return type;
	}

	public void setType(EventType _type) {
		type = _type;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String _sourceId) {
		sourceId = _sourceId;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator _operator) {
		operator = _operator;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double _value) {
		value = _value;
	}

	public boolean isOr() {
		return or;
	}

	public void setOr(boolean _or) {
		or = _or;
	}

	public List<Criteria> getCriteria() {
		return criteria;
	}

	public void setCriteria(List<Criteria> _criteria) {
		criteria = _criteria;
	}

	public EventId toEventId() {
		return new EventId(type, sourceId);
	}

	public Set<Integer> toJavaDays() {
		if (type != EventType.TIME)
			return Collections.emptySet();
		return CollectionUtils.transformToSet(CriteriaDay.toEnumSet(sourceId), _d->_d.javaDay);
	}

	public boolean isMet(List<Event> _events, TimeZone _tz) {
		Event e = CollectionUtils.filterOne(_events, this::triggers);
		if (type == EventType.TIME) {
			int day = DateUtils.toCalendar(new Date(), _tz).get(Calendar.DAY_OF_WEEK);
			if (!toJavaDays().contains(day))
				return false;
			Date timeToday = timeOfDay(new Date(), DaoSerializer.toInteger(getValue()), day, _tz);
			if (!e.getTime().equals(timeToday))
				return false;
		}
		else if (operator != null) {
			if (operator == Operator.GREATER) {
				if (e.getValue() <= value)
					return false;
			}
			else if (operator == Operator.GREATER_EQUAL) {
				if (e.getValue() < value)
					return false;
			}
			else if (operator == Operator.EQUAL) {
				if (e.getValue() != value)
					return false;
			}
			else if (operator == Operator.LESS_EQUAL) {
				if (e.getValue() > value)
					return false;
			}
			else if (operator == Operator.LESS) {
				if (e.getValue() >= value)
					return false;
			}
		}
		if (CollectionUtils.isNotEmpty(criteria)) {
			if (or)
				return CollectionUtils.anyQualify(criteria, _c->_c.isMet(_events, _tz));
			return CollectionUtils.allQualify(criteria, _c->_c.isMet(_events, _tz));
		}
		return true;
	}

	public boolean triggers(Event _event) {
		if (_event.getType() != type)
			return false;
		if (NullUtils.isEmpty(_event.getSourceId()) || NullUtils.isEqual(_event.getSourceId(), "*") || NullUtils.isEqual(_event.getSourceId(), sourceId))
			return true;
		return CollectionUtils.anyQualify(criteria, _c->_c.triggers(_event));
	}

	public void addAllCriteria(List<Criteria> _criteria) {
		_criteria.add(this);
		CollectionUtils.edit(criteria, _c->_c.addAllCriteria(_criteria));
	}

	public Date getNextTriggerDate(TimeZone _tz) {
		if (type != EventType.TIME)
			return null;
		Collection<Date> dates = CollectionUtils.transform(CriteriaDay.toEnumSet(getSourceId()), _s->nextTimeOfDay(new Date(), DaoSerializer.toInteger(getValue()), _s.javaDay, _tz));
		return CollectionUtils.getSmallest(dates);
	}

	public Date timeOfDay(Date _now, int _time, int _day, TimeZone _tz) {
		Calendar cal = DateUtils.toCalendar(_now, _tz);
		cal.set(Calendar.DAY_OF_WEEK, _day);
		cal.set(Calendar.HOUR_OF_DAY, hour(_time));
		cal.set(Calendar.MINUTE, minute(_time));
		cal.set(Calendar.SECOND, second(_time));
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public Date nextTimeOfDay(Date _now, int _time, int _day, TimeZone _tz) {
		Date time = timeOfDay(_now, _time, _day, _tz);
		return time.before(_now)?DateUtils.addDays(time,7, _tz):time;
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
}
