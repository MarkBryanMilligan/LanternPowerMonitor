package com.lanternsoftware.util.dao;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public class QueryFinalizerExecution<I,O> implements Callable<List<O>> {
    private final IDaoProxy proxy;
    private final Class<I> clazz;
    private final DaoQuery query;
    private final Collection<String> fields;
    private final DaoSort sort;
    private final QueryFinalizer<I, O> finalizer;

    public QueryFinalizerExecution(IDaoProxy _proxy, Class<I> _class, DaoQuery _query, Collection<String> _fields, DaoSort _sort, QueryFinalizer<I,O> _finalizer) {
        proxy = _proxy;
        clazz = _class;
        query = _query;
        fields = _fields;
        sort = _sort;
        finalizer = _finalizer;
    }

    @Override
    public List<O> call() {
        return finalizer.finalize(proxy, proxy.query(clazz, query, fields, sort));
    }
}
