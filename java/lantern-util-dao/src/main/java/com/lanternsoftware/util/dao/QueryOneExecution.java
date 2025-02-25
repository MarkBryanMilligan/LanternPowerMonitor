package com.lanternsoftware.util.dao;

import java.util.Collection;
import java.util.concurrent.Callable;

public class QueryOneExecution<V> implements Callable<V> {
    private final IDaoProxy proxy;
    private final Class<V> clazz;
    private final DaoQuery query;
    private final Collection<String> fields;
    private final DaoSort sort;

    public QueryOneExecution(IDaoProxy _proxy, Class<V> _class, DaoQuery _query) {
        this(_proxy, _class, _query, null, null);
    }

    public QueryOneExecution(IDaoProxy _proxy, Class<V> _class, DaoQuery _query, DaoSort _sort) {
        this(_proxy, _class, _query, null, _sort);
    }

    public QueryOneExecution(IDaoProxy _proxy, Class<V> _class, DaoQuery _query, Collection<String> _fields) {
        this(_proxy, _class, _query, _fields, null);
    }

    public QueryOneExecution(IDaoProxy _proxy, Class<V> _class, DaoQuery _query, Collection<String> _fields, DaoSort _sort) {
        proxy = _proxy;
        clazz = _class;
        query = _query;
        fields = _fields;
        sort = _sort;
    }

    @Override
    public V call() throws Exception {
        return proxy.queryOne(clazz, query, fields, sort);
    }
}
