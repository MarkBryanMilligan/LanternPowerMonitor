package com.lanternsoftware.datamodel.rules;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;

import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public enum CriteriaDay {
	SUN(Calendar.SUNDAY),
	MON(Calendar.MONDAY),
	TUE(Calendar.TUESDAY),
	WED(Calendar.WEDNESDAY),
	THU(Calendar.THURSDAY),
	FRI(Calendar.FRIDAY),
	SAT(Calendar.SATURDAY);

	public final int javaDay;

	CriteriaDay(int _javaDay) {
		javaDay = _javaDay;
	}

	public static String toString(Collection<CriteriaDay> _coll) {
		return CollectionUtils.transformToCommaSeparated(_coll, Enum::name, false);
	}

	public static EnumSet<CriteriaDay> toEnumSet(String _days) {
		String[] days = NullUtils.cleanSplit(_days, ",");
		Set<CriteriaDay> setDays = CollectionUtils.transformToSet(CollectionUtils.asArrayList(days), _s->NullUtils.toEnum(CriteriaDay.class, _s));
		return setDays.isEmpty()?EnumSet.allOf(CriteriaDay.class):EnumSet.copyOf(setDays);
	}
}
