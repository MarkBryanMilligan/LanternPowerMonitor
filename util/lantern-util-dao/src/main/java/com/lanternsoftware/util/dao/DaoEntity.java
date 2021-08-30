package com.lanternsoftware.util.dao;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.bson.Document;

public class DaoEntity implements Map<String, Object> {
    private final Document map;

    public DaoEntity() {
        map = new Document();
    }

    public DaoEntity(Document _doc) {
        map = (_doc == null) ? new Document() : _doc;
    }

    public DaoEntity(Map<String, ?> _map) {
        map = new Document();
        map.putAll(_map);
    }

    public DaoEntity(String _name, Object _o) {
        map = new Document();
        put(_name, _o);
    }

    public DaoEntity and(String _name, Object _o) {
        put(_name, _o);
        return this;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    public Document toDocument() {
        return map;
    }
}
