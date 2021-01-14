package com.lanternsoftware.util.dao;

import java.util.List;

public abstract class QueryFinalizer<I, O> {
    public abstract List<O> finalize(IDaoProxy _proxy, List<I> _input);
}
