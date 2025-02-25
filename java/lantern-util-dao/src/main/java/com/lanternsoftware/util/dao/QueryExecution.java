package com.lanternsoftware.util.dao;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public class QueryExecution<V> implements Callable<List<V>> {
    private final IDaoProxy proxy;
    private final Class<V> clazz;
    private final DaoQuery query;
    private final Collection<String> fields;
    private final DaoSort sort;

    public QueryExecution(IDaoProxy _proxy, Class<V> _class, DaoQuery _query) {
        this(_proxy, _class, _query, null, null);
    }

    public QueryExecution(IDaoProxy _proxy, Class<V> _class, DaoQuery _query, DaoSort _sort) {
        this(_proxy, _class, _query, null, _sort);
    }

    public QueryExecution(IDaoProxy _proxy, Class<V> _class, DaoQuery _query, Collection<String> _fields) {
        this(_proxy, _class, _query, _fields, null);
    }

    public QueryExecution(IDaoProxy _proxy, Class<V> _class, DaoQuery _query, Collection<String> _fields, DaoSort _sort) {
        proxy = _proxy;
        clazz = _class;
        query = _query;
        fields = _fields;
        sort = _sort;
    }

    @Override
    public List<V> call() throws Exception {
        return proxy.query(clazz, query, fields, sort);
    }
}
