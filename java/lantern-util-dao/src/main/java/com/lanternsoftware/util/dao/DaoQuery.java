package com.lanternsoftware.util.dao;

import java.util.Collection;
import java.util.Map;

import org.bson.Document;

public class DaoQuery extends Document {
    public DaoQuery() {
    }

    public DaoQuery(Map<String, Object> map) {
        super(map);
    }

    public DaoQuery(String _name, Object _o) {
        put(_name, _o);
    }

    public DaoQuery and(String _name, Object _o) {
        put(_name, _o);
        return this;
    }

    public DaoQuery or(DaoQuery _query) {
        put("$or", _query);
        return this;
    }

    public DaoQuery andIgnoreCase(String _name, Object _o) {
        put(_name, new DaoQuery("$equalIgnoreCase", _o));
        return this;
    }

    public DaoQuery andNotEquals(String _name, Object _o) {
        put(_name, new DaoQuery("$ne", _o));
        return this;
    }

    public DaoQuery andIn(String _name, Collection<String> _values) {
        put(_name, new DaoQuery("$in", _values));
        return this;
    }

    public DaoQuery andNotIn(String _name, Collection<String> _values) {
        put(_name, new DaoQuery("$nin", _values));
        return this;
    }

    public DaoQuery andInLongs(String _name, Collection<Long> _values) {
        put(_name, new DaoQuery("$in", _values));
        return this;
    }

    public DaoQuery andNotInLongs(String _name, Collection<Long> _values) {
        put(_name, new DaoQuery("$nin", _values));
        return this;
    }

    public DaoQuery andInIntegers(String _name, Collection<Integer> _values) {
        put(_name, new DaoQuery("$in", _values));
        return this;
    }

    public DaoQuery andNotInIntegers(String _name, Collection<Integer> _values) {
        put(_name, new DaoQuery("$nin", _values));
        return this;
    }

    public DaoQuery andGt(String _name, Object _o) {
        put(_name, new DaoQuery("$gt", _o));
        return this;
    }

    public DaoQuery andLt(String _name, Object _o) {
        put(_name, new DaoQuery("$lt", _o));
        return this;
    }

    public DaoQuery andGte(String _name, Object _o) {
        put(_name, new DaoQuery("$gte", _o));
        return this;
    }

    public DaoQuery andLte(String _name, Object _o) {
        put(_name, new DaoQuery("$lte", _o));
        return this;
    }

    public DaoQuery andBetween(String _name, Object _lowerBound, Object _upperBound) {
        put(_name, new DaoQuery("$gt", _lowerBound).and("$lt", _upperBound));
        return this;
    }

    public DaoQuery andBetweenInclusive(String _name, Object _lowerBound, Object _upperBound) {
        put(_name, new DaoQuery("$gte", _lowerBound).and("$lte", _upperBound));
        return this;
    }

    public DaoQuery andBetweenInclusiveExclusive(String _name, Object _lowerBound, Object _upperBound) {
        put(_name, new DaoQuery("$gte", _lowerBound).and("$lt", _upperBound));
        return this;
    }

    public DaoQuery andBetweenExclusiveInclusive(String _name, Object _lowerBound, Object _upperBound) {
        put(_name, new DaoQuery("$gt", _lowerBound).and("$lte", _upperBound));
        return this;
    }

    public DaoQuery andStartsWith(String _name, String _o) {
        put(_name, new DaoQuery("$startsWith", _o));
        return this;
    }

    public DaoQuery andStartsWithIgnoreCase(String _name, String _o) {
        put(_name, new DaoQuery("$startsWithIgnoreCase", _o));
        return this;
    }

    public DaoQuery andContains(String _name, String _o) {
        put(_name, new DaoQuery("$contains", _o));
        return this;
    }

    public DaoQuery andContainsIgnoreCase(String _name, String _o) {
        put(_name, new DaoQuery("$containsIgnoreCase", _o));
        return this;
    }

    public DaoQuery andNull(String _name) {
        put(_name, "$null");
        return this;
    }

    public DaoQuery andNotNull(String _name) {
        put(_name, "$notnull");
        return this;
    }

    public static DaoQuery notEquals(String _name, Object _value) {
        return new DaoQuery().andNotEquals(_name, _value);
    }

    public static DaoQuery in(String _name, Collection<String> _values) {
        return new DaoQuery().andIn(_name, _values);
    }

    public static DaoQuery notIn(String _name, Collection<String> _values) {
        return new DaoQuery().andNotIn(_name, _values);
    }

    public static DaoQuery inLongs(String _name, Collection<Long> _values) {
        return new DaoQuery().andInLongs(_name, _values);
    }

    public static DaoQuery notInLongs(String _name, Collection<Long> _values) {
        return new DaoQuery().andNotInLongs(_name, _values);
    }

    public static DaoQuery inIntegers(String _name, Collection<Integer> _values) {
        return new DaoQuery().andInIntegers(_name, _values);
    }

    public static DaoQuery notInIntegers(String _name, Collection<Integer> _values) {
        return new DaoQuery().andNotInIntegers(_name, _values);
    }

    public static DaoQuery gt(String _name, Object _value) {
        return new DaoQuery().andGt(_name, _value);
    }

    public static DaoQuery lt(String _name, Object _value) {
        return new DaoQuery().andLt(_name, _value);
    }

    public static DaoQuery gte(String _name, Object _value) {
        return new DaoQuery().andGte(_name, _value);
    }

    public static DaoQuery lte(String _name, Object _value) {
        return new DaoQuery().andLte(_name, _value);
    }

    public static DaoQuery between(String _name, Object _lowerBound, Object _upperBound) {
        return new DaoQuery().andBetween(_name, _lowerBound, _upperBound);
    }

    public static DaoQuery startsWith(String _name, String _value) {
        return new DaoQuery().andStartsWith(_name, _value);
    }

    public static DaoQuery startsWithIgnoreCase(String _name, String _value) {
        return new DaoQuery().andStartsWithIgnoreCase(_name, _value);
    }

    public static DaoQuery contains(String _name, String _value) {
        return new DaoQuery().andContains(_name, _value);
    }

    public static DaoQuery containsIgnoreCase(String _name, String _value) {
        return new DaoQuery().andContainsIgnoreCase(_name, _value);
    }

    public static DaoQuery isNull(String _name) {
        return new DaoQuery().andNull(_name);
    }

    public static DaoQuery notNull(String _name) {
        return new DaoQuery().andNotNull(_name);
    }
}
